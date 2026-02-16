$src = "app/src/main/assets/quran_tajweed_full.json"
$dest = "app/src/main/assets/juz_list_metadata.json"

Write-Host "Reading $src..."
$json = Get-Content $src -Encoding UTF8 -Raw | ConvertFrom-Json

$juzData = @{} # Key: JuzNumber, Value: Array of objects {surahNum, ayahNum}

Write-Host "Processing Ayahs..."
foreach ($surah in $json.data.surahs) {
    foreach ($ayah in $surah.ayahs) {
        $j = $ayah.juz
        if (-not $juzData.ContainsKey($j)) {
            $juzData[$j] = @()
        }
        $juzData[$j] += [PSCustomObject]@{
            surahNumber = $surah.number
            surahName   = $surah.englishName
            surahNameAr = $surah.name
            ayahNumber  = $ayah.numberInSurah
        }
    }
}

Write-Host "Consolidating Ranges..."
$finalList = @()
for ($j = 1; $j -le 30; $j++) {
    if ($juzData.ContainsKey($j)) {
        $entries = $juzData[$j]
        # Group by Surah
        $surahGroups = $entries | Group-Object surahNumber
        
        $surahList = @()
        foreach ($g in $surahGroups) {
            $sNum = $g.Group[0].surahNumber
            $sName = $g.Group[0].surahName
            $sNameAr = $g.Group[0].surahNameAr
            # Find min and max ayah
            $min = ($g.Group | Measure-Object -Property ayahNumber -Minimum).Minimum
            $max = ($g.Group | Measure-Object -Property ayahNumber -Maximum).Maximum
            
            $surahList += [PSCustomObject]@{
                surahNumber = $sNum
                surahName   = $sName
                arabicName  = $sNameAr
                ayahRange   = "$min-$max"
            }
        }
        
        $finalList += [PSCustomObject]@{
            number = $j
            surahs = $surahList
        }
    }
}

Write-Host "Saving to $dest..."
$finalList | ConvertTo-Json -Depth 4 | Out-File $dest -Encoding UTF8
Write-Host "Done."
