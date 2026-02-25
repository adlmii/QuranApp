"""
Fix footnote remnants in translation text.
The Quran.com API returns translations with HTML like:
  <sup foot_note=123456>1</sup>
After HTML tag stripping, the number '1' remains stuck to the text.
This script removes those remnant footnote numbers.
"""
import sqlite3
import re
import shutil

conn = sqlite3.connect('scripts/quran.db')

rows = conn.execute('SELECT id, translation_id FROM ayah').fetchall()

# Pattern to match <sup ...>N</sup> remnants:
# These are 1-2 digit numbers that appear after a letter/punctuation
# and are NOT part of a real number in the text
# Examples: "bertakwa,1" -> "bertakwa," | "Mīm.1" -> "Mīm." | "menginfakkan1" -> "menginfakkan"
footnote_pattern = re.compile(r'(?<=[^\d\s])\d{1,2}(?=\s|$|[,.])')

updated = 0
for row_id, text in rows:
    cleaned = footnote_pattern.sub('', text)
    if cleaned != text:
        conn.execute('UPDATE ayah SET translation_id = ? WHERE id = ?', (cleaned, row_id))
        updated += 1

conn.commit()
print(f"Cleaned footnote numbers from {updated} ayahs")

# Show some samples
samples = conn.execute(
    "SELECT surah_id, verse_number, translation_id FROM ayah WHERE surah_id=2 LIMIT 5"
).fetchall()
for s in samples:
    end = s[2][-60:] if len(s[2]) > 60 else s[2]
    print(f"  {s[0]}:{s[1]} -> ...{end}")

conn.close()

# Copy to assets
shutil.copy('scripts/quran.db', 'app/src/main/assets/database/quran.db')
print("Copied to assets/database/quran.db")
