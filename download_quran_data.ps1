$base = "https://raw.githubusercontent.com/semarketir/quranjson/master/source"
$dest = "app/src/main/assets/data"

New-Item -ItemType Directory -Force "$dest"
New-Item -ItemType Directory -Force "$dest/surah"
New-Item -ItemType Directory -Force "$dest/translation"
New-Item -ItemType Directory -Force "$dest/tajweed"

# Metadata
Write-Host "Downloading Metadata..."
curl.exe -L -o "$dest/surah.json" "$base/surah.json"

# Surah 1 & 18
foreach ($id in @(1, 18)) {
    Write-Host "Downloading Surah $id..."
    curl.exe -L -o "$dest/surah/surah_$id.json" "$base/surah/surah_$id.json"
    curl.exe -L -o "$dest/translation/id_translation_$id.json" "$base/translation/id/id_translation_$id.json"
    curl.exe -L -o "$dest/tajweed/surah_$id.json" "$base/tajweed/surah_$id.json"
}
Write-Host "Download Complete."
