"""
generate_quran_db.py
====================
Fetches all Quran data from Quran.com API v4 and generates a pre-populated
SQLite database (quran.db) ready for use with Android Room's createFromAsset().

Tables:
  - surah: 114 rows (id, name_simple, name_arabic, revelation_place, verses_count)
  - ayah:  6236 rows (id, surah_id, verse_number, page_number, juz_number,
                       text_uthmani, translation_id)

Usage:
    python generate_quran_db.py
"""

import json
import os
import sqlite3
import sys
import time
import urllib.request
import urllib.error

BASE_URL = "https://api.quran.com/api/v4"
TRANSLATION_ID = 33  # Kemenag RI Indonesian translation
PER_PAGE = 50  # API max per page for verses
RATE_LIMIT_SECONDS = 0.5
OUTPUT_FILE = "quran.db"


def api_get(endpoint: str, params: dict = None) -> dict:
    """Make a GET request to the Quran.com API v4 with retry logic."""
    url = f"{BASE_URL}/{endpoint}"
    if params:
        query = "&".join(f"{k}={v}" for k, v in params.items())
        url = f"{url}?{query}"

    max_retries = 3
    for attempt in range(max_retries):
        try:
            req = urllib.request.Request(url)
            req.add_header("Accept", "application/json")
            req.add_header("User-Agent", "QuranAppDBGenerator/1.0")
            with urllib.request.urlopen(req, timeout=30) as resp:
                return json.loads(resp.read().decode("utf-8"))
        except (urllib.error.URLError, urllib.error.HTTPError, TimeoutError) as e:
            print(f"  ⚠ Attempt {attempt + 1}/{max_retries} failed for {url}: {e}")
            if attempt < max_retries - 1:
                wait = (attempt + 1) * 2
                print(f"    Retrying in {wait}s...")
                time.sleep(wait)
            else:
                raise RuntimeError(f"Failed to fetch {url} after {max_retries} attempts") from e


def create_schema(conn: sqlite3.Connection):
    """Create the database tables and indices.
    
    IMPORTANT: Schema must match Room's expected format exactly:
    - PRIMARY KEY columns need explicit NOT NULL
    - Index names must use Room convention: index_tableName_columnName
    - No FOREIGN KEY constraints (not declared in Room @Entity)
    """
    conn.executescript("""
        CREATE TABLE IF NOT EXISTS surah (
            id INTEGER NOT NULL PRIMARY KEY,
            name_simple TEXT NOT NULL,
            name_arabic TEXT NOT NULL,
            revelation_place TEXT NOT NULL,
            verses_count INTEGER NOT NULL
        );

        CREATE TABLE IF NOT EXISTS ayah (
            id INTEGER NOT NULL PRIMARY KEY,
            surah_id INTEGER NOT NULL,
            verse_number INTEGER NOT NULL,
            page_number INTEGER NOT NULL,
            juz_number INTEGER NOT NULL,
            text_uthmani TEXT NOT NULL,
            translation_id TEXT NOT NULL
        );

        CREATE INDEX IF NOT EXISTS index_ayah_surah_id ON ayah(surah_id);
        CREATE INDEX IF NOT EXISTS index_ayah_page_number ON ayah(page_number);
        CREATE INDEX IF NOT EXISTS index_ayah_juz_number ON ayah(juz_number);
    """)


def fetch_chapters(conn: sqlite3.Connection):
    """Fetch all 114 surah metadata and insert into the surah table."""
    print("\n📖 Fetching surah metadata...")
    data = api_get("chapters", {"language": "id"})
    chapters = data["chapters"]

    rows = []
    for ch in chapters:
        rows.append((
            ch["id"],
            ch["name_simple"],
            ch["name_arabic"],
            ch["revelation_place"],  # "makkah" or "madinah"
            ch["verses_count"],
        ))

    conn.executemany(
        "INSERT OR REPLACE INTO surah (id, name_simple, name_arabic, revelation_place, verses_count) "
        "VALUES (?, ?, ?, ?, ?)",
        rows,
    )
    conn.commit()
    print(f"  ✅ Inserted {len(rows)} surahs")
    return chapters


def fetch_verses(conn: sqlite3.Connection, chapters: list):
    """Fetch all ayahs for each surah with text, page, juz, and translation."""
    print("\n📜 Fetching ayah data (this may take a few minutes)...\n")

    global_ayah_id = 0
    total_inserted = 0

    for ch in chapters:
        chapter_id = ch["id"]
        chapter_name = ch["name_simple"]
        expected_verses = ch["verses_count"]

        print(f"  [{chapter_id:3d}/114] {chapter_name} ({expected_verses} ayat)...", end=" ", flush=True)

        all_verses = []
        page = 1

        while True:
            time.sleep(RATE_LIMIT_SECONDS)

            data = api_get("verses/by_chapter/" + str(chapter_id), {
                "language": "id",
                "translations": str(TRANSLATION_ID),
                "fields": "text_uthmani,page_number,juz_number",
                "per_page": str(PER_PAGE),
                "page": str(page),
            })

            verses = data.get("verses", [])
            all_verses.extend(verses)

            pagination = data.get("pagination", {})
            total_pages = pagination.get("total_pages", 1)

            if page >= total_pages:
                break
            page += 1

        # Insert verses
        rows = []
        for v in all_verses:
            global_ayah_id += 1

            # Extract translation text
            translation_text = ""
            translations = v.get("translations", [])
            if translations:
                translation_text = translations[0].get("text", "")
                # Clean HTML tags from translation (API sends <sup foot_note=X>N</sup> etc.)
                import re
                translation_text = re.sub(r"<[^>]+>", "", translation_text)
                # Remove footnote number remnants (digits left after stripping <sup> tags)
                translation_text = re.sub(r'(?<=[^\d\s])\d{1,2}(?=\s|$|[,.])', '', translation_text)

            rows.append((
                global_ayah_id,
                chapter_id,
                v["verse_number"],
                v.get("page_number", 0),
                v.get("juz_number", 0),
                v.get("text_uthmani", ""),
                translation_text,
            ))

        conn.executemany(
            "INSERT OR REPLACE INTO ayah "
            "(id, surah_id, verse_number, page_number, juz_number, text_uthmani, translation_id) "
            "VALUES (?, ?, ?, ?, ?, ?, ?)",
            rows,
        )
        conn.commit()
        total_inserted += len(rows)

        status = "✅" if len(rows) == expected_verses else f"⚠ got {len(rows)}"
        print(status)

    print(f"\n  📊 Total ayahs inserted: {total_inserted}")
    return total_inserted


def verify_database(db_path: str):
    """Run verification queries on the generated database."""
    print("\n🔍 Verifying database...\n")

    conn = sqlite3.connect(db_path)
    c = conn.cursor()

    surah_count = c.execute("SELECT COUNT(*) FROM surah").fetchone()[0]
    ayah_count = c.execute("SELECT COUNT(*) FROM ayah").fetchone()[0]
    page_range = c.execute("SELECT MIN(page_number), MAX(page_number) FROM ayah").fetchone()
    juz_range = c.execute("SELECT MIN(juz_number), MAX(juz_number) FROM ayah").fetchone()
    first_verse = c.execute(
        "SELECT text_uthmani FROM ayah WHERE surah_id=1 AND verse_number=1"
    ).fetchone()[0]
    last_verse = c.execute(
        "SELECT text_uthmani FROM ayah WHERE surah_id=114 ORDER BY verse_number DESC LIMIT 1"
    ).fetchone()[0]
    sample_translation = c.execute(
        "SELECT translation_id FROM ayah WHERE surah_id=1 AND verse_number=1"
    ).fetchone()[0]

    file_size_mb = round(os.path.getsize(db_path) / (1024 * 1024), 2)

    print(f"  Surah count     : {surah_count} {'✅' if surah_count == 114 else '❌'}")
    print(f"  Ayah count      : {ayah_count} {'✅' if ayah_count == 6236 else '❌'}")
    print(f"  Page range      : {page_range[0]} - {page_range[1]} {'✅' if page_range == (1, 604) else '❌'}")
    print(f"  Juz range       : {juz_range[0]} - {juz_range[1]} {'✅' if juz_range == (1, 30) else '❌'}")
    print(f"  First verse     : {first_verse[:50]}...")
    print(f"  Last verse (114): {last_verse[:50]}...")
    print(f"  Translation (1:1): {sample_translation[:60]}...")
    print(f"  File size       : {file_size_mb} MB")

    # Check per-surah verse counts match
    mismatches = c.execute("""
        SELECT s.id, s.name_simple, s.verses_count, COUNT(a.id) as actual
        FROM surah s
        LEFT JOIN ayah a ON a.surah_id = s.id
        GROUP BY s.id
        HAVING s.verses_count != actual
    """).fetchall()

    if mismatches:
        print(f"\n  ⚠ Verse count mismatches: {len(mismatches)}")
        for m in mismatches:
            print(f"    Surah {m[0]} ({m[1]}): expected {m[2]}, got {m[3]}")
    else:
        print(f"  Verse counts    : All 114 surahs match ✅")

    conn.close()

    all_ok = (
        surah_count == 114
        and ayah_count == 6236
        and page_range == (1, 604)
        and juz_range == (1, 30)
        and not mismatches
    )

    if all_ok:
        print(f"\n🎉 Database generated successfully! ({file_size_mb} MB)")
    else:
        print(f"\n⚠ Database generated with warnings. Please review above.")

    return all_ok


def main():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    db_path = os.path.join(script_dir, OUTPUT_FILE)

    # Remove old database if exists
    if os.path.exists(db_path):
        os.remove(db_path)
        print(f"🗑  Removed old {OUTPUT_FILE}")

    print(f"🚀 Generating {OUTPUT_FILE}...")
    print(f"   API Base: {BASE_URL}")
    print(f"   Translation: Kemenag RI (ID: {TRANSLATION_ID})")

    conn = sqlite3.connect(db_path)
    conn.execute("PRAGMA journal_mode=WAL")
    conn.execute("PRAGMA synchronous=NORMAL")

    try:
        create_schema(conn)
        chapters = fetch_chapters(conn)
        fetch_verses(conn, chapters)
        conn.close()
        verify_database(db_path)
    except Exception as e:
        conn.close()
        print(f"\n❌ Error: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
