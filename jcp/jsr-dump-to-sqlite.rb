# encoding: UTF-8
require 'csv'
require "sqlite3"

db = SQLite3::Database.new "jsr-list-origin.sqlite"

db.execute <<-SQL
  CREATE TABLE IF NOT EXISTS jsr (
    id INTEGER PRIMARY KEY,
    no VARCHAR(16) NOT NULL UNIQUE,
    status VARCHAR(32),
    start_date DATE,
    end_date DATE,
    effective_date DATE,
    platform VARCHAR(32),
    summary TEXT,
    tags VARCHAR(256)
  );
SQL

CSV.foreach("./jsr-sanitized.csv", headers: true) do |row|
  no, status, start_date, end_date, effective_date, platform, summary, tags = row.fields
  db.execute("INSERT INTO jsr (no, status, start_date, end_date, effective_date, platform, summary) 
    VALUES (?, ?, ?, ?, ?, ?, ?)", ["JSR-" + no, status, start_date, end_date, effective_date, platform, summary])
end