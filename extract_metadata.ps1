$src = "app/src/main/assets/quran_tajweed_full.json"
$dest = "app/src/main/assets/surah_list_metadata.json"

Write-Host "Reading $src..."
$json = Get-Content $src -Encoding UTF8 -Raw | ConvertFrom-Json

Write-Host "Extracting Metadata..."
$meta = $json.data.surahs | ForEach-Object {
    [PSCustomObject]@{
        number                 = $_.number
        name                   = $_.name
        englishName            = $_.englishName
        englishNameTranslation = $_.englishNameTranslation
        numberOfAyahs          = $_.ayahs.Count
        revelationType         = $_.revelationType
    }
}

Write-Host "Saving to $dest..."
$meta | ConvertTo-Json -Depth 2 | Out-File $dest -Encoding UTF8

Write-Host "Done."
