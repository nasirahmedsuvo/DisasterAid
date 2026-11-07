Add-Type -AssemblyName System.Drawing
$imagePath = 'C:\Users\user\Downloads\Gemini_Generated_Image_nug2a5nug2a5nug2.jpg'
if (!(Test-Path $imagePath)) {
    Write-Error "Source image not found at $imagePath"
    exit 1
}
$sourceImg = [System.Drawing.Image]::FromFile($imagePath)

# 1. Generate Mipmap launcher icons (.png)
$sizes = @{
    'mipmap-mdpi' = 48
    'mipmap-hdpi' = 72
    'mipmap-xhdpi' = 96
    'mipmap-xxhdpi' = 144
    'mipmap-xxxhdpi' = 192
}

foreach ($pair in $sizes.GetEnumerator()) {
    $folder = $pair.Key
    $size = $pair.Value
    $dir = 'C:\Users\user\AndroidStudioProjects\DisasterAid\app\src\main\res\' + $folder
    if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }

    $bmp = New-Object System.Drawing.Bitmap $size, $size
    $gfx = [System.Drawing.Graphics]::FromImage($bmp)
    $gfx.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $gfx.DrawImage($sourceImg, 0, 0, $size, $size)

    $bmp.Save("$dir\ic_launcher.png", [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Save("$dir\ic_launcher_round.png", [System.Drawing.Imaging.ImageFormat]::Png)

    $gfx.Dispose()
    $bmp.Dispose()
}

# 2. Generate in-app logo drawable (ic_app_logo.png)
$drawableDir = 'C:\Users\user\AndroidStudioProjects\DisasterAid\app\src\main\res\drawable'
if (!(Test-Path $drawableDir)) { New-Item -ItemType Directory -Path $drawableDir | Out-Null }
$logoBmp = New-Object System.Drawing.Bitmap 512, 512
$logoGfx = [System.Drawing.Graphics]::FromImage($logoBmp)
$logoGfx.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$logoGfx.DrawImage($sourceImg, 0, 0, 512, 512)
$logoBmp.Save("$drawableDir\ic_app_logo.png", [System.Drawing.Imaging.ImageFormat]::Png)
$logoGfx.Dispose()
$logoBmp.Dispose()

# 3. Generate adaptive icon foreground (ic_launcher_foreground.png - 432x432)
$fgBmp = New-Object System.Drawing.Bitmap 432, 432
$fgGfx = [System.Drawing.Graphics]::FromImage($fgBmp)
$fgGfx.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$fgGfx.DrawImage($sourceImg, 0, 0, 432, 432)
$fgBmp.Save("$drawableDir\ic_launcher_foreground.png", [System.Drawing.Imaging.ImageFormat]::Png)
$fgGfx.Dispose()
$fgBmp.Dispose()

$sourceImg.Dispose()
Write-Host 'All app icons, logos, and adaptive foregrounds successfully generated!'
