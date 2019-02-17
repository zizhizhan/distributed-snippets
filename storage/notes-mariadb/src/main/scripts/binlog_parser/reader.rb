# Integer       |         |
# Directive     | Returns | Meaning
# ------------------------------------------------------------------
# C             | Integer | 8-bit unsigned (unsigned char)
# S             | Integer | 16-bit unsigned, native endian (uint16_t)
# L             | Integer | 32-bit unsigned, native endian (uint32_t)
# Q             | Integer | 64-bit unsigned, native endian (uint64_t)
# J             | Integer | pointer width unsigned, native endian (uintptr_t)
#               |         |
# c             | Integer | 8-bit signed (signed char)
# s             | Integer | 16-bit signed, native endian (int16_t)
# l             | Integer | 32-bit signed, native endian (int32_t)
# q             | Integer | 64-bit signed, native endian (int64_t)
# j             | Integer | pointer width signed, native endian (intptr_t)
#               |         |
# S_ S!         | Integer | unsigned short, native endian
# I I_ I!       | Integer | unsigned int, native endian
# L_ L!         | Integer | unsigned long, native endian
# Q_ Q!         | Integer | unsigned long long, native endian (ArgumentError
#               |         | if the platform has no long long type.)
# J!            | Integer | uintptr_t, native endian (same with J)
#               |         |
# s_ s!         | Integer | signed short, native endian
# i i_ i!       | Integer | signed int, native endian
# l_ l!         | Integer | signed long, native endian
# q_ q!         | Integer | signed long long, native endian (ArgumentError
#               |         | if the platform has no long long type.)
# j!            | Integer | intptr_t, native endian (same with j)
#               |         |
# S> s> S!> s!> | Integer | same as the directives without ">" except
# L> l> L!> l!> |         | big endian
# I!> i!>       |         |
# Q> q> Q!> q!> |         | "S>" is same as "n"
# J> j> J!> j!> |         | "L>" is same as "N"
#               |         |
# S< s< S!< s!< | Integer | same as the directives without "<" except
# L< l< L!< l!< |         | little endian
# I!< i!<       |         |
# Q< q< Q!< q!< |         | "S<" is same as "v"
# J< j< J!< j!< |         | "L<" is same as "V"
#               |         |
# n             | Integer | 16-bit unsigned, network (big-endian) byte order
# N             | Integer | 32-bit unsigned, network (big-endian) byte order
# v             | Integer | 16-bit unsigned, VAX (little-endian) byte order
# V             | Integer | 32-bit unsigned, VAX (little-endian) byte order
#               |         |
# U             | Integer | UTF-8 character
# w             | Integer | BER-compressed integer (see Array.pack)
#
# Float        |         |
# Directive    | Returns | Meaning
# -----------------------------------------------------------------
# D d          | Float   | double-precision, native format
# F f          | Float   | single-precision, native format
# E            | Float   | double-precision, little-endian byte order
# e            | Float   | single-precision, little-endian byte order
# G            | Float   | double-precision, network (big-endian) byte order
# g            | Float   | single-precision, network (big-endian) byte order
#
# String       |         |
# Directive    | Returns | Meaning
# -----------------------------------------------------------------
# A            | String  | arbitrary binary string (remove trailing nulls and ASCII spaces)
# a            | String  | arbitrary binary string
# Z            | String  | null-terminated string
# B            | String  | bit string (MSB first)
# b            | String  | bit string (LSB first)
# H            | String  | hex string (high nibble first)
# h            | String  | hex string (low nibble first)
# u            | String  | UU-encoded string
# M            | String  | quoted-printable, MIME encoding (see RFC2045)
# m            | String  | base64 encoded string (RFC 2045) (default)
#              |         | base64 encoded string (RFC 4648) if followed by 0
# P            | String  | pointer to a structure (fixed-length string)
# p            | String  | pointer to a null-terminated string
#
# Misc.        |         |
# Directive    | Returns | Meaning
# -----------------------------------------------------------------
# @            | ---     | skip to the offset given by the length argument
# X            | ---     | skip backward one byte
# x            | ---     | skip forward one byte

class Reader
  def initialize(io)
    @io = io
  end

  def read(len)
    @io.read(len)
  end

  def end?
    @io.eof?
  end

  def position
    @io.tell
  end

  def seek(pos)
    @io.seek(pos)
  end

  def remaining(next_position)
    next_position - @io.tell
  end

  # Read an unsigned 8-bit (1-byte) integer.
  def read_uint8
    @io.read(1).unpack("C").first
  end

  # Read an unsigned 16-bit (2-byte) integer.
  def read_uint16
    @io.read(2).unpack("S").first
  end

  # Read an unsigned 24-bit (3-byte) integer.
  def read_uint24
    a, b, c = @io.read(3).unpack("CCC")
    a + (b << 8) + (c << 16)
  end

  # Read an unsigned 32-bit (4-byte) integer.
  def read_uint32
    @io.read(4).unpack("L").first
  end

  # Read an unsigned 40-bit (5-byte) integer.
  def read_uint40
    a, b = @io.read(5).unpack("CV")
    a + (b << 8)
  end

  # Read an unsigned 48-bit (6-byte) integer.
  def read_uint48
    a, b, c = @io.read(6).unpack("vvv")
    a + (b << 16) + (c << 32)
  end

  # Read an unsigned 56-bit (7-byte) integer.
  def read_uint56
    a, b, c = @io.read(7).unpack("CvV")
    a + (b << 8) + (c << 24)
  end

  # Read an unsigned 64-bit (8-byte) integer.
  def read_uint64
    @io.read(8).unpack("Q").first
  end

  # Read a signed 8-bit (1-byte) integer.
  def read_int8
    @io.read(1).unpack("c").first
  end

  # Read a signed 16-bit (2-byte) integer.
  def read_int16
    @io.read(2).unpack("s").first
  end

  # Read a signed 32-bit (4-byte) integer.
  def read_int32
    @io.read(4).unpack("l").first
  end

  # Read a signed 64-bit (8-byte) integer.
  def read_int32
    @io.read(8).unpack("q").first
  end

  # Read a signed 16-bit (2-byte) big-endian integer.
  def read_int16_be
    @io.read(2).reverse.unpack("s").first
  end

  def read_uint16_be
    io.read(2).unpack("n").first
  end

  # Read a signed 24-bit (3-byte) big-endian integer.
  def read_int24_be
    a, b, c = @io.read(3).unpack("CCC")
    if (a & 128) == 0
      (a << 16) | (b << 8) | c
    else
      (-1 << 24) | (a << 16) | (b << 8) | c
    end
  end

  # Read a signed 32-bit (4-byte) big-endian integer.
  def read_int32_be
    @io.read(4).reverse.unpack("l").first
  end

  def read_uint32_be
    @io.read(4).unpack("N").first
  end

  # Read a single-precision (4-byte) floating point number.
  def read_float
    @io.read(4).unpack("e").first
  end

  # Read a double-precision (8-byte) floating point number.
  def read_double
    @io.read(8).unpack("E").first
  end

  # Read an array of unsigned 8-bit (1-byte) integers.
  def read_uint8_array(length)
    @io.read(length).bytes.to_a
  end

  # Read an arbitrary-length bitmap, provided its length. Returns an array
  # of true/false values. This is used both for internal usage in RBR
  # events that need bitmaps, as well as for the BIT type.
  def read_bit_array(length)
    data = @io.read((length + 7) / 8)
    data.unpack("b*").first.  # Unpack into a string of "10101"
      split("").map { |c| c == "1" }.shift(length) # Return true/false array
  end

  # Read a non-terminated string, provided its length.
  def read_nstring(length)
    @io.read(length)
  end

  # Read a null-terminated string, provided its length (with the null).
  def read_nstringz(length)
    @io.read(length).unpack("A*").first
  end

  # Read a (Pascal-style) length-prefixed string. The length is stored as a
  # 8-bit (1-byte) to 32-bit (4-byte) unsigned integer, depending on the
  # optional size parameter (default 1), followed by the string itself with
  # no termination character.
  def read_lpstring(size = 1)
    length = read_uint_by_size(size)
    read_nstring(length)
  end

  # Read an lpstring (as above) which is also terminated with a null byte.
  def read_lpstringz(size = 1)
    string = read_lpstring(size)
    @io.read(1) # null
    string
  end

  def read_utf8
    length = @io.read(2).unpack("n").first
    @io.read(length).unpack("a*").first
  end

  def read_uint_by_size(size)
    case size
    when 1
      read_uint8
    when 2
      read_uint16
    when 3
      read_uint24
    when 4
      read_uint32
    when 5
      read_uint40
    when 6
      read_uint48
    when 7
      read_uint56
    when 8
      read_uint64
    end
  end

  # Read a uint value using the provided size, and convert it to an array
  # of symbols derived from a mapping table provided.
  def read_uint_bitmap_by_size_and_name(size, bit_names)
    value = read_uint_by_size(size)
    named_bits = []

    # Do an efficient scan for the named bits we know about using the hash
    # provided.
    bit_names.each do |(name, bit_value)|
      if (value & bit_value) != 0
        value -= bit_value
        named_bits << name
      end
    end

    # If anything is left over in +value+, add "unknown" names to the result
    # so that they can be identified and corrected.
    if value > 0
      0.upto(size * 8).map { |n| 1 << n }.each do |bit_value|
        if (value & bit_value) != 0
          named_bits << "unknown_#{bit_value}".to_sym
        end
      end
    end

    named_bits
  end
end
