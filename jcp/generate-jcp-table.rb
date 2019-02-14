# encoding: UTF-8

require 'nokogiri'
require 'open-uri'

url = "https://jcp.org/en/jsr/all"
doc = Nokogiri::HTML(open(url))
doc.encoding = "utf-8"

dates_pattern = /(Start|End|Effective)\:\s*(\d\d\d\d-\d\d-\d\d)/i

open './jsr-list.md', 'w:UTF-8' do |io|
    io << "# List of all JSRs\n"
    io << "\nNo | Status | Start | End | Effective | Name\n"
    io << "--- | --- | --- | --- | --- | ---\n"
    doc.css("table.listBy_table").each do |table|
        id = table.at("td.listBy_tableTitle:first").text
        status = table.at("tr:nth-child(3) td span").text
        name = table.at("td.listBy_tableTitle a").inner_html
        dates = {}
        table.at('table').css("td").each do |td|
            s = td.text
            m = dates_pattern.match(s)
            dates[m[1]] = m[2] if m
        end
        io << "JSR-#{'%03d' % id.to_i} | #{'%-12s' % status} | #{'%10s' % dates['Start']} | #{'%10s' % dates['End']} | #{'%10s' % dates['Effective']} | #{name.gsub("|", "&#124;")}\n"
    end
end

