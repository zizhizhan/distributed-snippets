require_relative "reader"

# Enumerated in sql/log_event.h line ~539 as Log_event_type
EVENT_TYPES_HASH = {
  :unknown_event => 0,  #
  :start_event_v3 => 1,  # (deprecated)
  :query_event => 2,  #
  :stop_event => 3,  #
  :rotate_event => 4,  #
  :intvar_event => 5,  #
  :load_event => 6,  # (deprecated)
  :slave_event => 7,  # (deprecated)
  :create_file_event => 8,  # (deprecated)
  :append_block_event => 9,  #
  :exec_load_event => 10,  # (deprecated)
  :delete_file_event => 11,  #
  :new_load_event => 12,  # (deprecated)
  :rand_event => 13,  #
  :user_var_event => 14,  #
  :format_description_event => 15,  #
  :xid_event => 16,  #
  :begin_load_query_event => 17,  #
  :execute_load_query_event => 18,  #
  :table_map_event => 19,  #
  :pre_ga_write_rows_event => 20,  # (deprecated)
  :pre_ga_update_rows_event => 21,  # (deprecated)
  :pre_ga_delete_rows_event => 22,  # (deprecated)
  :write_rows_event => 23,  #
  :update_rows_event => 24,  #
  :delete_rows_event => 25,  #
  :incident_event => 26,  #
  :heartbeat_log_event => 27,  #
  :table_metadata_event => 50,  # Only in Twitter MySQL
}

# A lookup array to map an integer event type ID to its symbol.
EVENT_TYPES = EVENT_TYPES_HASH.inject(Array.new(256)) do |type_array, item|
  type_array[item[1]] = item[0]
  type_array
end

# Values for the +flags+ field that may appear in binary logs. There are
# several other values that never appear in a file but may be used
# in events in memory.
#
# Defined in sql/log_event.h line ~448
EVENT_HEADER_FLAGS = {
  :binlog_in_use => 0x01, # LOG_EVENT_BINLOG_IN_USE_F
  :thread_specific => 0x04, # LOG_EVENT_THREAD_SPECIFIC_F
  :suppress_use => 0x08, # LOG_EVENT_SUPPRESS_USE_F
  :artificial => 0x20, # LOG_EVENT_ARTIFICIAL_F
  :relay_log => 0x40, # LOG_EVENT_RELAY_LOG_F
}

# A mapping array for all values that may appear in the +status+ field of
# a query_event.
#
# Defined in sql/log_event.h line ~316
QUERY_EVENT_STATUS_TYPES = [
  :flags2,                    #  0 (Q_FLAGS2_CODE)
  :sql_mode,                  #  1 (Q_SQL_MODE_CODE)
  :catalog_deprecated,        #  2 (Q_CATALOG_CODE)
  :auto_increment,            #  3 (Q_AUTO_INCREMENT)
  :charset,                   #  4 (Q_CHARSET_CODE)
  :time_zone,                 #  5 (Q_TIME_ZONE_CODE)
  :catalog,                   #  6 (Q_CATALOG_NZ_CODE)
  :lc_time_names,             #  7 (Q_LC_TIME_NAMES_CODE)
  :charset_database,          #  8 (Q_CHARSET_DATABASE_CODE)
  :table_map_for_update,      #  9 (Q_TABLE_MAP_FOR_UPDATE_CODE)
  :master_data_written,       # 10 (Q_MASTER_DATA_WRITTEN_CODE)
  :invoker,                   # 11 (Q_INVOKER)
]

# A mapping hash for all values that may appear in the +flags2+ field of
# a query_event.
#
# Defined in sql/log_event.h line ~521 in OPTIONS_WRITTEN_TO_BIN_LOG
#
# Defined in sql/sql_priv.h line ~84
QUERY_EVENT_FLAGS2 = {
  :auto_is_null => 1 << 14, # OPTION_AUTO_IS_NULL
  :not_autocommit => 1 << 19, # OPTION_NOT_AUTOCOMMIT
  :no_foreign_key_checks => 1 << 26, # OPTION_NO_FOREIGN_KEY_CHECKS
  :relaxed_unique_checks => 1 << 27, # OPTION_RELAXED_UNIQUE_CHECKS
}

class BinlogParser
  MAGIC_SIZE = 4
  MAGIC_VALUE = 0x6E6962FE

  def parse(file)
    open file, "r:UTF-8" do |io|
      reader = Reader.new(io)
      magic = reader.read_uint32

      if magic == MAGIC_VALUE
        read_event(reader)
      end
    end
  end

  def read_event(reader)
    while true
      break if reader.end?
      header = read_event_header(reader)
      break if header.nil?

      if @fde
        reader.seek(reader.position + @fde[:header_length])
      end

      puts header
      fields = read_event_fields(reader, header)

      case header[:event_type]
      when :format_description_event
        process_fde(fields)
      end
    end
  end

  def read_event_header(reader)
    header = {}
    header[:timestamp] = reader.read_uint32
    header[:event_type] = EVENT_TYPES[reader.read_uint8]
    header[:server_id] = reader.read_uint32
    header[:event_length] = reader.read_uint32
    header[:next_position] = reader.read_uint32
    header[:flags] = reader.read_uint_bitmap_by_size_and_name(2, EVENT_HEADER_FLAGS)
    header
  end

  def read_event_fields(reader, header)
    next_position = header[:next_position]
    event_type = header[:event_type]
    if event_type
      if self.respond_to?(event_type)
        fields = self.send(event_type, reader)
        if event_type == :query_event
          query_length = reader.remaining(next_position)
          fields[:query] = reader.read(query_length)            
        end
        puts fields
      end
    end
    reader.seek(next_position)
    fields
  end

  def process_fde(fde)
    @fde = {
      :header_length => fde[:header_length],
      :binlog_version => fde[:binlog_version],
      :server_version => fde[:server_version],
    }
  end

  def format_description_event(reader)
    fields = {}
    fields[:binlog_version] = reader.read_uint16
    fields[:server_version] = reader.read_nstringz(50)
    fields[:create_timestamp] = reader.read_uint32
    fields[:header_length] = reader.read_uint8
    fields
  end

  def query_event(reader)
    fields = {}
    fields[:thread_id] = reader.read_uint32
    fields[:elapsed_time] = reader.read_uint32
    db_length = reader.read_uint8
    fields[:error_code] = reader.read_uint16
    puts fields
    fields
  end

  
end

if __FILE__ == $0
  parser = BinlogParser.new
  puts parser.parse("/tmp/mysql/master/log/master-bin.000001")
end
