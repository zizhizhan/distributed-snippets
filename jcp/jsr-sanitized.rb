# encoding: UTF-8
require 'csv'

data_pattern = /^JSR\-(\d+)\s\|([^|]+)\|([^|]+)\|([^|]+)\|([^|]+)\|([^|]+)\|([^|]+)$/
# ([^\|]+)|([^\|]+)|([^\|]+)|([^\|]+)|(.+)$/

CSV.open("./jsr-sanitized.csv", 'w:UTF-8') do |csv|
  csv << %w[no status start end effective platform name group]
  open('./jsr-sanitized.md', 'r:UTF-8').each do |line|
    m = data_pattern.match(line.strip)
    if m 
        csv << [m[1], m[2], m[3], m[4], m[5], m[6], m[7], nil]
    end
  end
end