param(
    [string]$ProjectRoot = (Split-Path $PSScriptRoot -Parent)
)

$ErrorActionPreference = 'Stop'

function Get-LocalProperty([string]$Name) {
    $propertyPath = Join-Path $ProjectRoot 'local.properties'
    $line = Get-Content -LiteralPath $propertyPath | Where-Object { $_ -match ('^' + [regex]::Escape($Name) + '=') } | Select-Object -First 1
    if (-not $line) {
        throw "$Name is missing from local.properties."
    }
    $line.Substring($line.IndexOf('=') + 1).Trim()
}

function Get-CapitalEnglish([string]$Raw) {
    $parentheticalValues = [regex]::Matches($Raw, '\(([^()]*)\)')
    foreach ($parentheticalValue in $parentheticalValues) {
        $value = $parentheticalValue.Groups[1].Value.Trim()
        if ($value -notmatch '[A-Za-z]') {
            continue
        }
        if ($value -match '^Washington,\s*D\.C') {
            return 'Washington, D.C.'
        }
        $parts = @($value -split ',' | ForEach-Object { $_.Trim() })
        $latinPart = $parts | Where-Object { $_ -match '^[A-Za-zÀ-ž'' .-]+$' } | Select-Object -Last 1
        if ($latinPart) {
            return $latinPart
        }
        return ([regex]::Match($value, '[A-Za-zÀ-ž][A-Za-zÀ-ž'' .-]*').Value.Trim())
    }
    ''
}

function Get-PrimaryCapital([string]$Raw) {
    if ([string]::IsNullOrWhiteSpace($Raw)) {
        return ''
    }
    $value = ($Raw -split '\(')[0].Trim()
    $value = ($value -split '[,※]')[0].Trim()
    $value.Trim(' ', ',', ')')
}

$regionGroups = [ordered]@{
    '아시아' = 'AE AF AM AZ BD BH BN BT CN CY GE ID IL IN IQ IR JO JP KG KH KP KR KZ KW LA LB LK MM MN MV MY NP OM PH PK PS QA SA SG SY TH TJ TL TM TR TW UZ VN YE'.Split(' ')
    '유럽' = 'AD AL AT BA BE BG BY CH CZ DE DK EE ES FI FR GB GR HR HU IE IS IT LI LT LU LV MC MD ME MK MT NL NO PL PT RO RS RU SE SI SK SM UA VA XK'.Split(' ')
    '아프리카' = 'AO BF BI BJ BW CD CF CG CI CM CV DJ DZ EG ER ET GA GH GM GN GQ GW KE KM LR LS LY MA MG ML MR MU MW MZ NA NE NG RW SC SD SL SN SO SS ST SZ TD TG TN TZ UG ZA ZM ZW'.Split(' ')
    '북아메리카' = 'AG BB BS BZ CA CR CU DM DO GD GT HN HT JM KN LC MX NI PA SV TT US VC'.Split(' ')
    '남아메리카' = 'AR BO BR CL CO EC GY PE PY SR UY VE'.Split(' ')
    '오세아니아' = 'AU CK FJ FM KI MH NR NU NZ PG PW SB TO TV VU WS'.Split(' ')
}

$regionByCode = @{}
foreach ($entry in $regionGroups.GetEnumerator()) {
    foreach ($code in $entry.Value) {
        $regionByCode[$code] = $entry.Key
    }
}

$easyCodes = 'AU BR CA CH CN DE EG ES FR GB GR ID IN IT JP KR MX NL NO NZ PH RU SA SE SG TH TR US VN ZA'.Split(' ')
$mediumCodes = 'AE AR AT BE CL CO CZ DK FI HU IE IL IS KP MA MY PE PL PT QA TW UA'.Split(' ')

$capitalOverrides = @{
    'BJ' = [ordered]@{ primary = '포르토노보'; english = 'Porto-Novo'; aliases = @('포르토노보', '포르토 노보', '코토누', 'Porto-Novo', 'Cotonou') }
    'BO' = [ordered]@{ primary = '수크레'; english = 'Sucre'; aliases = @('수크레', '라파스', 'Sucre', 'La Paz') }
    'CI' = [ordered]@{ primary = '야무수크로'; english = 'Yamoussoukro'; aliases = @('야무수크로', '아비장', 'Yamoussoukro', 'Abidjan') }
    'IL' = [ordered]@{ primary = '예루살렘'; english = 'Jerusalem'; aliases = @('예루살렘', 'Jerusalem') }
    'KZ' = [ordered]@{ primary = '아스타나'; english = 'Astana'; aliases = @('아스타나', 'Astana') }
    'LK' = [ordered]@{ primary = '스리자야와르데네푸라코테'; english = 'Sri Jayewardenepura Kotte'; aliases = @('스리자야와르데네푸라코테', '스리 자야와르데네푸라 코테', '콜롬보', 'Sri Jayewardenepura Kotte', 'Colombo') }
    'PS' = [ordered]@{ primary = '라말라'; english = 'Ramallah'; aliases = @('라말라', 'Ramallah') }
    'UA' = [ordered]@{ primary = '키이우'; english = 'Kyiv'; aliases = @('키이우', '키예프', 'Kyiv', 'Kiev') }
    'US' = [ordered]@{ primary = '워싱턴 D.C.'; english = 'Washington, D.C.'; aliases = @('워싱턴', '워싱턴 D.C.', '워싱턴DC', 'Washington', 'Washington, D.C.') }
    'VA' = [ordered]@{ primary = '바티칸'; english = 'Vatican City'; aliases = @('바티칸', '바티칸시국', 'Vatican', 'Vatican City') }
    'TW' = [ordered]@{ primary = '타이베이'; english = 'Taipei'; aliases = @('타이베이', '타이페이', 'Taipei') }
    'ZA' = [ordered]@{ primary = '프리토리아'; english = 'Pretoria'; aliases = @('프리토리아', '케이프타운', '블룸폰테인', 'Pretoria', 'Cape Town', 'Bloemfontein') }
}

$countryAliases = @{
    'AE' = @('UAE', '아랍에미리트')
    'GB' = @('영국', 'United Kingdom', 'UK')
    'KP' = @('북한', '조선민주주의인민공화국', 'North Korea', 'DPRK')
    'KR' = @('대한민국', '한국', '남한', 'Republic of Korea', 'South Korea', 'Korea')
    'TR' = @('튀르키예', '터키', 'Türkiye', 'Turkey')
    'TW' = @('대만', '타이완', 'Taiwan')
    'US' = @('미국', '미합중국', 'United States', 'USA')
    'VA' = @('바티칸', '바티칸시국', '교황청')
    'VE' = @('베네수엘라', '베네수엘라볼리바르')
}

$manualCountries = @(
    [ordered]@{
        iso2 = 'KR'
        countryKo = '대한민국'
        countryEn = 'Republic of Korea'
        countryAliases = @('대한민국', '한국', '남한', 'Republic of Korea', 'South Korea', 'Korea')
        capitalKo = '서울'
        capitalEn = 'Seoul'
        capitalAliases = @('서울', 'Seoul')
        capitalRaw = '서울(Seoul)'
        population = 0
        area = 0.0
    },
    [ordered]@{
        iso2 = 'KP'
        countryKo = '북한'
        countryEn = "Democratic People's Republic of Korea"
        countryAliases = @('북한', '조선민주주의인민공화국', 'North Korea', 'DPRK')
        capitalKo = '평양'
        capitalEn = 'Pyongyang'
        capitalAliases = @('평양', 'Pyongyang')
        capitalRaw = '평양(Pyongyang)'
        population = 0
        area = 0.0
    }
)

$serviceKey = Get-LocalProperty 'DATA_GO_KR_SERVICE_KEY'
$requestUri = 'https://apis.data.go.kr/1262000/OverviewGnrlInfoService/getOverviewGnrlInfoList?serviceKey=' + $serviceKey + '&pageNo=1&numOfRows=300'
$response = Invoke-RestMethod -Method Get -Uri $requestUri
$header = $response.response.header
if ([string]$header.resultCode -ne '0') {
    throw "API request failed: $($header.resultCode) $($header.resultMsg)"
}

$flagDirectory = Join-Path $ProjectRoot 'app\src\main\res\drawable-nodpi'
$flagCodes = @(Get-ChildItem -LiteralPath $flagDirectory -File | ForEach-Object {
    $match = [regex]::Match($_.BaseName, '^flag_([a-z]{2})$')
    if ($match.Success) {
        $match.Groups[1].Value.ToUpperInvariant()
    }
} | Where-Object { $_ } | Sort-Object -Unique)

$countries = @()
foreach ($item in @($response.response.body.items.item)) {
    $iso = ([string]$item.country_iso_alp2).Trim().ToUpperInvariant()
    if ($iso -notin $flagCodes) {
        continue
    }
    if (-not $regionByCode.ContainsKey($iso)) {
        throw "No region mapping for $iso."
    }
    $capitalRaw = ([string]$item.capital).Trim()
    $override = $capitalOverrides[$iso]
    if ($override -and [string]::IsNullOrWhiteSpace($capitalRaw)) {
        $capitalRaw = "$($override.primary)($($override.english))"
    }
    $capitalKo = if ($override) { $override.primary } else { Get-PrimaryCapital $capitalRaw }
    $capitalEn = if ($override) { $override.english } else { Get-CapitalEnglish $capitalRaw }
    if ([string]::IsNullOrWhiteSpace($capitalKo)) {
        throw "No capital parsed for $iso."
    }
    $capitalAliases = @($capitalKo)
    if ($capitalEn) {
        $capitalAliases += $capitalEn
    }
    if ($override) {
        $capitalAliases += @($override.aliases)
    }
    $capitalAliases = @($capitalAliases | Where-Object { $_ } | Select-Object -Unique)
    $aliases = @([string]$item.country_nm, [string]$item.country_eng_nm)
    if ($countryAliases.ContainsKey($iso)) {
        $aliases += @($countryAliases[$iso])
    }
    $difficulty = if ($iso -in $easyCodes) { 1 } elseif ($iso -in $mediumCodes) { 2 } else { 3 }
    $countries += [ordered]@{
        iso2 = $iso
        countryKo = ([string]$item.country_nm).Trim()
        countryEn = ([string]$item.country_eng_nm).Trim()
        countryAliases = @($aliases | Where-Object { $_ } | Select-Object -Unique)
        capitalKo = $capitalKo
        capitalEn = $capitalEn
        capitalAliases = $capitalAliases
        capitalRaw = $capitalRaw
        flagResName = 'flag_' + $iso.ToLowerInvariant()
        region = $regionByCode[$iso]
        difficulty = $difficulty
        population = if ($null -eq $item.population) { 0 } else { [long]$item.population }
        area = if ($null -eq $item.area) { 0.0 } else { [double]$item.area }
        quizEnabled = $true
    }
}

$existingCodes = @($countries | ForEach-Object { $_.iso2 })
foreach ($manual in $manualCountries) {
    $iso = $manual.iso2
    if ($iso -notin $flagCodes -or $iso -in $existingCodes) {
        continue
    }
    $countries += [ordered]@{
        iso2 = $iso
        countryKo = $manual.countryKo
        countryEn = $manual.countryEn
        countryAliases = @($manual.countryAliases)
        capitalKo = $manual.capitalKo
        capitalEn = $manual.capitalEn
        capitalAliases = @($manual.capitalAliases)
        capitalRaw = $manual.capitalRaw
        flagResName = 'flag_' + $iso.ToLowerInvariant()
        region = $regionByCode[$iso]
        difficulty = if ($iso -in $easyCodes) { 1 } elseif ($iso -in $mediumCodes) { 2 } else { 3 }
        population = [long]$manual.population
        area = [double]$manual.area
        quizEnabled = $true
    }
}

$countries = @($countries | Sort-Object { $_.countryKo })
if ($countries.Count -ne $flagCodes.Count) {
    throw "Country count $($countries.Count) does not match flag count $($flagCodes.Count)."
}

$payload = [ordered]@{
    version = (Get-Date -Format 'yyyy.MM.dd')
    source = '외교부 국가·지역별 일반사항 및 보완 데이터'
    sourceUrl = 'https://www.data.go.kr/data/15099534/openapi.do'
    generatedAt = (Get-Date).ToString('o')
    totalCount = $countries.Count
    countries = $countries
}

$assetDirectory = Join-Path $ProjectRoot 'app\src\main\assets'
[IO.Directory]::CreateDirectory($assetDirectory) | Out-Null
$outputPath = Join-Path $assetDirectory 'countries.json'
$json = $payload | ConvertTo-Json -Depth 8
[IO.File]::WriteAllText($outputPath, $json, [Text.UTF8Encoding]::new($false))

$resourceDirectory = Join-Path $ProjectRoot 'app\src\main\java\com\chlqudco\countryquiz\data'
[IO.Directory]::CreateDirectory($resourceDirectory) | Out-Null
$resourcePath = Join-Path $resourceDirectory 'FlagResources.kt'
$resourceLines = @(
    'package com.chlqudco.countryquiz.data',
    '',
    'import com.chlqudco.countryquiz.R',
    '',
    'object FlagResources {',
    '    fun id(iso2: String): Int = when (iso2.uppercase()) {'
)
foreach ($code in $flagCodes) {
    $resourceLines += '        "' + $code + '" -> R.drawable.flag_' + $code.ToLowerInvariant()
}
$resourceLines += @(
    '        else -> 0',
    '    }',
    '}',
    ''
)
[IO.File]::WriteAllLines($resourcePath, $resourceLines, [Text.UTF8Encoding]::new($false))

Write-Output "Generated $($countries.Count) countries at $outputPath"
Write-Output "Generated $($flagCodes.Count) flag references at $resourcePath"
