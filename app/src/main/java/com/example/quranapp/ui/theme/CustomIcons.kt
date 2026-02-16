package com.example.quranapp.ui.theme

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

// Custom Icon for Rukuk (Praying position)
val RukukIcon: ImageVector
    get() = if (_rukukIcon != null) _rukukIcon!! else {
        _rukukIcon = materialIcon(name = "Rukuk") {
            materialPath {
                // Head (circle-ish) at (17, 7)
                moveTo(17.0f, 7.0f)
                curveToRelative(0.0f, -1.66f, -1.34f, -3.0f, -3.0f, -3.0f)
                reflectiveCurveToRelative(-3.0f, 1.34f, -3.0f, 3.0f)
                reflectiveCurveToRelative(1.34f, 3.0f, 3.0f, 3.0f)
                reflectiveCurveToRelative(3.0f, -1.34f, 3.0f, -3.0f)
                close()

                // Body (Rukuk Posture - Facing Right)
                // Back horizontal at y=12, Legs vertical at x=8
                moveTo(14.0f, 11.0f) // Shoulder/Neck joint
                lineTo(7.0f, 11.0f)  // Back (horizontal)
                lineTo(7.0f, 21.0f)  // Legs (vertical)
                lineTo(9.5f, 21.0f)  // Leg width
                lineTo(9.5f, 13.5f)  // Hip/Waist (under)
                lineTo(14.0f, 13.5f) // Torso width (under)
                // Arm extending down to knee
                lineTo(14.0f, 17.0f) // Hand reaching down
                lineTo(16.5f, 17.0f) // Hand width
                lineTo(16.5f, 11.0f) // Arm back up/Shoulder
                close()
            }
        }
        _rukukIcon!!
    }

private var _rukukIcon: ImageVector? = null

// Custom Icon for Abstract Calligraphy (Decorative)
val CalligraphyIcon: ImageVector
    get() = if (_calligraphyIcon != null) _calligraphyIcon!! else {
        _calligraphyIcon = materialIcon(name = "Calligraphy") {
            materialPath {
                // Abstract stylized script shape (flowing lines)
                moveTo(18.0f, 4.0f)
                curveTo(16.0f, 4.0f, 14.0f, 5.0f, 13.0f, 7.0f)
                curveTo(12.0f, 9.0f, 13.0f, 11.0f, 14.5f, 12.0f)
                curveTo(16.0f, 13.0f, 17.0f, 14.5f, 16.0f, 16.0f)
                curveTo(15.0f, 17.5f, 13.0f, 18.0f, 11.0f, 18.0f)
                curveTo(9.0f, 18.0f, 7.0f, 17.0f, 6.0f, 15.0f)
                lineTo(4.0f, 16.0f)
                curveTo(5.5f, 19.0f, 8.5f, 20.0f, 11.0f, 20.0f)
                curveTo(14.5f, 20.0f, 17.5f, 18.5f, 18.5f, 15.5f)
                curveTo(19.5f, 12.5f, 17.5f, 10.0f, 15.5f, 9.0f)
                curveTo(14.5f, 8.5f, 14.0f, 7.5f, 14.5f, 6.5f)
                curveTo(15.0f, 5.5f, 16.5f, 5.5f, 18.0f, 5.5f)
                lineTo(18.0f, 4.0f)
                close()
                
                // Dot/Accent
                moveTo(12.0f, 13.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                reflectiveCurveToRelative(-0.45f, -1.0f, -1.0f, -1.0f)
                reflectiveCurveToRelative(-1.0f, 0.45f, -1.0f, 1.0f)
                reflectiveCurveToRelative(0.45f, 1.0f, 1.0f, 1.0f)
                close()
            }
        }
        _calligraphyIcon!!
    }

private var _calligraphyIcon: ImageVector? = null
