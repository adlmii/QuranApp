$dest = "app/src/main/assets"

Write-Host "Downloading Tajweed Data..."
curl.exe -L -o "$dest/quran_tajweed_full.json" "http://api.alquran.cloud/v1/quran/quran-tajweed"

Write-Host "Downloading Translation Data..."
curl.exe -L -o "$dest/quran_translation_full.json" "http://api.alquran.cloud/v1/quran/id.indonesian"

Write-Host "Download Complete."
