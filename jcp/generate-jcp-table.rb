# encoding: UTF-8

require 'nokogiri'
require 'open-uri'

url = "https://jcp.org/en/jsr/all"
doc = Nokogiri::HTML(open(url))
doc.encoding = "utf-8"

open './jsr-list.md', 'w:UTF-8' do |io|
    io << "# List of all JSRs\n"
    io << "\nNo | Name\n"
    io << "--- | ---\n"
    doc.css("table.listBy_table").each do |table|
        id = table.at("td.listBy_tableTitle:first").text
        name = table.at("td.listBy_tableTitle a").inner_html
        io << "JSR-#{'%03d' % id.to_i} | #{name.gsub("|", "&#124;")}\n"
    end
end

