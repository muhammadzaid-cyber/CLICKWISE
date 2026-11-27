package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SecurityUpdateGood
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.UpdatePreferenceSnapshot
import com.example.domain.model.AppLanguage
import com.example.domain.update.UpdateUiState
import com.example.ui.theme.LocalClickwiseColors
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UpdateAvailableDialog(
    visible: Boolean,
    state: UpdateUiState,
    installedVersionName: String,
    installedVersionCode: Int,
    language: AppLanguage,
    reduceMotion: Boolean,
    onDownloadAndInstall: () -> Unit,
    onSelectLater: () -> Unit,
    onCancelDownload: () -> Unit,
    onOpenPermissionSettings: () -> Unit,
    onLaunchSystemInstaller: () -> Unit,
    onRetry: () -> Unit,
    onClose: () -> Unit
) {
    if (!visible) return

    val colors = LocalClickwiseColors.current
    val isDismissibleOutside = state !is UpdateUiState.Downloading && state !is UpdateUiState.Verifying

    // Automatically hand off to the Android package installer as soon as verification & permission pass.
    LaunchedEffect(state) {
        if (state is UpdateUiState.ReadyToInstall) {
            onLaunchSystemInstaller()
        }
    }

    Dialog(
        onDismissRequest = {
            if (isDismissibleOutside) {
                if (state is UpdateUiState.UpdateAvailable) onSelectLater() else onClose()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = isDismissibleOutside,
            dismissOnClickOutside = isDismissibleOutside
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            TechnicalPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 540.dp)
                    .testTag("update_dialog"),
                borderColor = when (state) {
                    is UpdateUiState.Error -> colors.alertRed.copy(alpha = 0.65f)
                    is UpdateUiState.InstallSucceeded -> colors.limeSignal.copy(alpha = 0.65f)
                    else -> colors.borderCyan
                },
                containerColor = colors.bgSecondary,
                cornerRadius = 18.dp,
                contentPadding = PaddingValues(22.dp),
                showCornerReticles = true,
                accentRailColor = when (state) {
                    is UpdateUiState.Error -> colors.alertRed
                    is UpdateUiState.InstallSucceeded -> colors.limeSignal
                    else -> colors.cyanPrimary
                }
            ) {
                AnimatedContent(
                    targetState = state::class.simpleName ?: "UpdateState",
                    transitionSpec = {
                        if (reduceMotion) {
                            fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                        } else {
                            fadeIn(tween(180)) togetherWith fadeOut(tween(140))
                        }
                    },
                    label = "updateDialogContent"
                ) { _ ->
                    when (state) {
                        is UpdateUiState.UpdateAvailable -> {
                            UpdateAvailableContent(
                                state = state,
                                installedVersionName = installedVersionName,
                                installedVersionCode = installedVersionCode,
                                language = language,
                                onDownloadAndInstall = onDownloadAndInstall,
                                onSelectLater = onSelectLater
                            )
                        }

                        is UpdateUiState.Downloading -> {
                            UpdateDownloadingContent(
                                state = state,
                                language = language,
                                onCancel = onCancelDownload
                            )
                        }

                        is UpdateUiState.Verifying -> {
                            UpdateVerifyingContent(
                                state = state,
                                language = language
                            )
                        }

                        is UpdateUiState.PermissionRequired -> {
                            UpdatePermissionContent(
                                state = state,
                                language = language,
                                onOpenSettings = onOpenPermissionSettings,
                                onCancel = onClose
                            )
                        }

                        is UpdateUiState.ReadyToInstall,
                        is UpdateUiState.Installing -> {
                            val versionName = when (state) {
                                is UpdateUiState.ReadyToInstall -> state.update.metadata.versionName
                                is UpdateUiState.Installing -> state.update.metadata.versionName
                                else -> ""
                            }
                            UpdateReadyToInstallContent(
                                versionName = versionName,
                                isLaunching = state is UpdateUiState.Installing,
                                language = language,
                                onLaunchInstaller = onLaunchSystemInstaller,
                                onClose = onClose
                            )
                        }

                        is UpdateUiState.InstallSucceeded -> {
                            UpdateSuccessContent(
                                versionName = state.versionName,
                                language = language,
                                onClose = onClose
                            )
                        }

                        is UpdateUiState.Error -> {
                            UpdateErrorContent(
                                state = state,
                                language = language,
                                onRetry = onRetry,
                                onClose = onClose
                            )
                        }

                        else -> {
                            Spacer(Modifier.height(1.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateAvailableContent(
    state: UpdateUiState.UpdateAvailable,
    installedVersionName: String,
    installedVersionCode: Int,
    language: AppLanguage,
    onDownloadAndInstall: () -> Unit,
    onSelectLater: () -> Unit
) {
    val colors = LocalClickwiseColors.current
    val update = state.update
    val isUrdu = language == AppLanguage.UR

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionEyebrow(
                text = if (isUrdu) "تصدیق شدہ گٹ ہب ریلیز" else "VERIFIED GITHUB RELEASE"
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colors.cyanPrimary.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, colors.borderCyan)
            ) {
                Text(
                    text = if (isUrdu) "ورژن ${update.metadata.versionName}" else "Version ${update.metadata.versionName}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.cyanPrimary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.cyanPrimary.copy(alpha = 0.14f))
                    .border(1.dp, colors.borderCyan, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SystemUpdate,
                    contentDescription = null,
                    tint = colors.cyanPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = if (isUrdu) "CLICKWISE کا نیا ورژن دستیاب ہے" else "A NEW CLICKWISE VERSION IS AVAILABLE",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.textPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (isUrdu) "آپ کا سائبر سیکیورٹی معاون مزید بہتر ہو گیا ہے۔" else "Your cybersecurity assistant just got better.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = colors.surfaceDeep,
            border = BorderStroke(1.dp, colors.borderMuted),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "v$installedVersionName ($installedVersionCode) → v${update.metadata.versionName} (${update.metadata.versionCode})",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textPrimary
                )
                if (update.apkSizeBytes > 0L) {
                    Text(
                        text = formatByteSize(update.apkSizeBytes),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.cyanPrimary
                    )
                }
            }
        }

        if (update.isBelowMinimumSupported) {
            Spacer(Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = colors.warnAmber.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, colors.warnAmber.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isUrdu) {
                        "توصیہ: آپ کا موجودہ ورژن کم از کم مطلوبہ ورژن سے پرانا ہے۔ براہِ کرم اپ ڈیٹ کریں۔"
                    } else {
                        "Recommended security update: your installed build is below the minimum supported version."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.warnAmber,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            text = if (isUrdu) "نیا کیا ہے" else "WHAT'S NEW",
            style = MaterialTheme.typography.labelMedium,
            color = colors.cyanPrimary
        )
        Spacer(Modifier.height(6.dp))

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = colors.bgPrimary.copy(alpha = 0.85f),
            border = BorderStroke(1.dp, colors.borderMuted),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp, max = 180.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    text = update.metadata.releaseNotes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.VerifiedUser,
                contentDescription = null,
                tint = colors.limeSignal,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "SHA-256: ${update.metadata.sha256.take(16)}… • ${update.metadata.apkAssetName}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary
            )
        }

        Spacer(Modifier.height(18.dp))

        Button(
            onClick = onDownloadAndInstall,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .minimumInteractiveComponentSize()
                .testTag("update_dialog_install_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.cyanPrimary,
                contentColor = colors.bgPrimary
            )
        ) {
            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (isUrdu) "ڈاؤن لوڈ اور انسٹال کریں" else "DOWNLOAD & INSTALL UPDATE",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onSelectLater,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .minimumInteractiveComponentSize()
                .testTag("update_dialog_later_button")
        ) {
            Text(
                text = if (isUrdu) "بعد میں" else "LATER",
                style = MaterialTheme.typography.labelLarge,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun UpdateDownloadingContent(
    state: UpdateUiState.Downloading,
    language: AppLanguage,
    onCancel: () -> Unit
) {
    val colors = LocalClickwiseColors.current
    val isUrdu = language == AppLanguage.UR
    val percent = (state.progressFraction * 100).toInt()

    Column {
        SectionEyebrow(text = if (isUrdu) "محفوظ ڈاؤن لوڈ جاری ہے" else "SECURE APK DOWNLOAD")
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (isUrdu) "CLICKWISE v${state.update.metadata.versionName} ڈاؤن لوڈ ہو رہا ہے" else "Downloading CLICKWISE v${state.update.metadata.versionName}",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.textPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (isUrdu) {
                "تنصیب سے پہلے فائل کی SHA-256 اور دستخطی تصدیق کی جائے گی۔"
            } else {
                "Downloading over HTTPS from GitHub Releases. SHA-256 integrity will be verified before install."
            },
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )

        Spacer(Modifier.height(18.dp))

        if (state.totalBytes > 0L) {
            LinearProgressIndicator(
                progress = { state.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = colors.cyanPrimary,
                trackColor = colors.surfaceDeep
            )
        } else {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = colors.cyanPrimary,
                trackColor = colors.surfaceDeep
            )
        }

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (state.totalBytes > 0L) {
                    "${formatByteSize(state.bytesDownloaded)} / ${formatByteSize(state.totalBytes)}"
                } else {
                    formatByteSize(state.bytesDownloaded)
                },
                style = MaterialTheme.typography.labelMedium,
                color = colors.textPrimary
            )
            Text(
                text = if (state.totalBytes > 0L) "$percent%" else "...",
                style = MaterialTheme.typography.labelMedium,
                color = colors.cyanPrimary
            )
        }

        Spacer(Modifier.height(18.dp))
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .minimumInteractiveComponentSize()
                .testTag("update_cancel_download_button"),
            border = BorderStroke(1.dp, colors.borderMuted),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = if (isUrdu) "ڈاؤن لوڈ منسوخ کریں" else "CANCEL DOWNLOAD",
                style = MaterialTheme.typography.labelLarge,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun UpdateVerifyingContent(
    state: UpdateUiState.Verifying,
    language: AppLanguage
) {
    val colors = LocalClickwiseColors.current
    val isUrdu = language == AppLanguage.UR

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        CircularProgressIndicator(
            color = colors.cyanPrimary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(44.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (isUrdu) "پیکیج کی سیکیورٹی تصدیق جاری ہے" else "VERIFYING UPDATE INTEGRITY",
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (isUrdu) {
                "SHA-256 چیک سم، پیکیج شناخت اور دستخطی سرٹیفکیٹ کی جانچ ہو رہی ہے..."
            } else {
                "Checking SHA-256 checksum, package ID, version ${state.update.metadata.versionCode}, and signing certificate..."
            },
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun UpdatePermissionContent(
    state: UpdateUiState.PermissionRequired,
    language: AppLanguage,
    onOpenSettings: () -> Unit,
    onCancel: () -> Unit
) {
    val colors = LocalClickwiseColors.current
    val isUrdu = language == AppLanguage.UR

    Column {
        SectionEyebrow(
            text = if (isUrdu) "اینڈرائیڈ سیکیورٹی اجازت درکار" else "ANDROID INSTALL AUTHORIZATION",
            accentColor = colors.warnAmber
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.SettingsApplications,
                contentDescription = null,
                tint = colors.warnAmber,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = if (isUrdu) "ایپ اپ ڈیٹ انسٹال کرنے کی اجازت دیں" else "Allow CLICKWISE to Install Updates",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (isUrdu) {
                "اینڈرائیڈ کی پالیسی کے تحت تصدیق شدہ CLICKWISE.apk انسٹالر کھولنے سے پہلے آپ کی اجازت ضروری ہے۔ آپ کی موجودہ ایپ اور ترتیبات محفوظ رہیں گی۔"
            } else {
                "Android requires your permission to let CLICKWISE launch the system package installer for this verified update. Your existing settings and progress will be preserved."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )

        if (state.previouslyDenied) {
            Spacer(Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = colors.warnAmber.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, colors.warnAmber.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isUrdu) {
                        "اجازت ابھی فعال نہیں کی گئی۔ براہِ کرم سیٹنگز میں جا کر \"Allow from this source\" آن کریں۔"
                    } else {
                        "Install permission is still disabled. Enable \"Allow from this source\" in Android Settings and return here to continue."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.warnAmber,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        Spacer(Modifier.height(18.dp))
        Button(
            onClick = onOpenSettings,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .minimumInteractiveComponentSize()
                .testTag("update_open_settings_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.cyanPrimary,
                contentColor = colors.bgPrimary
            )
        ) {
            Text(
                text = if (isUrdu) "اینڈرائیڈ سیٹنگز کھولیں" else "OPEN PERMISSION SETTINGS",
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .minimumInteractiveComponentSize()
                .testTag("update_close_button")
        ) {
            Text(
                text = if (isUrdu) "منسوخ کریں" else "CANCEL",
                style = MaterialTheme.typography.labelLarge,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun UpdateReadyToInstallContent(
    versionName: String,
    isLaunching: Boolean,
    language: AppLanguage,
    onLaunchInstaller: () -> Unit,
    onClose: () -> Unit
) {
    val colors = LocalClickwiseColors.current
    val isUrdu = language == AppLanguage.UR

    Column {
        SectionEyebrow(
            text = if (isUrdu) "تصدیق مکمل" else "CHECKSUM & SIGNATURE VERIFIED",
            accentColor = colors.limeSignal
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.SecurityUpdateGood,
                contentDescription = null,
                tint = colors.limeSignal,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = if (isUrdu) "ورژن $versionName انسٹال کے لیے تیار ہے" else "Ready to Install Version $versionName",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (isUrdu) {
                if (isLaunching) "اینڈرائیڈ سسٹم انسٹالر کھولا جا رہا ہے۔ براہِ کرم سسٹم اسکرین پر اپ ڈیٹ کی منظوری دیں۔"
                else "اپ ڈیٹ کی تصدیق ہو چکی ہے۔ انسٹال شروع کرنے کے لیے نیچے بٹن دبائیں۔"
            } else {
                if (isLaunching) "Opening the Android system installer. Approve the prompt to update CLICKWISE in place without losing your data."
                else "Verification passed. Tap below to launch the Android system package installer."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = onLaunchInstaller,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .minimumInteractiveComponentSize()
                .testTag("update_launch_installer_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.limeSignal,
                contentColor = colors.bgPrimary
            )
        ) {
            Text(
                text = if (isUrdu) "انسٹالر کھولیں" else "OPEN ANDROID INSTALLER",
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .minimumInteractiveComponentSize()
                .testTag("update_close_button")
        ) {
            Text(
                text = if (isUrdu) "بند کریں" else "CLOSE",
                style = MaterialTheme.typography.labelLarge,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun UpdateSuccessContent(
    versionName: String,
    language: AppLanguage,
    onClose: () -> Unit
) {
    val colors = LocalClickwiseColors.current
    val isUrdu = language == AppLanguage.UR

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = colors.limeSignal,
            modifier = Modifier.size(44.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (isUrdu) "اپ ڈیٹ مکمل ہو گئی" else "Update Applied ($versionName)",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.textPrimary
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .minimumInteractiveComponentSize()
                .testTag("update_close_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.cyanPrimary,
                contentColor = colors.bgPrimary
            )
        ) {
            Text(if (isUrdu) "مکمل" else "DONE", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun UpdateErrorContent(
    state: UpdateUiState.Error,
    language: AppLanguage,
    onRetry: () -> Unit,
    onClose: () -> Unit
) {
    val colors = LocalClickwiseColors.current
    val isUrdu = language == AppLanguage.UR

    Column {
        SectionEyebrow(
            text = if (isUrdu) "اپ ڈیٹ میں رکاوٹ" else "UPDATE ATTENTION REQUIRED",
            accentColor = colors.alertRed
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = colors.alertRed,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = if (isUrdu) "اپ ڈیٹ مکمل نہ ہو سکی" else "Update Could Not Complete",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary
            )
        }
        Spacer(Modifier.height(10.dp))
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = colors.alertRed.copy(alpha = 0.10f),
            border = BorderStroke(1.dp, colors.alertRed.copy(alpha = 0.40f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isUrdu) state.messageUr else state.message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary,
                modifier = Modifier.padding(12.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (isUrdu) {
                "آپ کی موجودہ CLICKWISE ایپ اور مقامی ترجیحات مکمل طور پر محفوظ ہیں۔"
            } else {
                "Your existing CLICKWISE installation and saved preferences remain safe and unchanged."
            },
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .minimumInteractiveComponentSize()
                .testTag("update_retry_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.cyanPrimary,
                contentColor = colors.bgPrimary
            )
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (isUrdu) "دوبارہ کوشش کریں" else "RETRY SAFELY",
                style = MaterialTheme.typography.labelLarge
            )
        }
        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .minimumInteractiveComponentSize()
                .testTag("update_close_button")
        ) {
            Text(
                text = if (isUrdu) "بند کریں" else "CLOSE",
                style = MaterialTheme.typography.labelLarge,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
fun AppUpdateStatusPanel(
    language: AppLanguage,
    installedVersionName: String,
    installedVersionCode: Int,
    updateState: UpdateUiState,
    preferences: UpdatePreferenceSnapshot,
    onCheckForUpdates: () -> Unit,
    onOpenUpdateDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    val isUrdu = language == AppLanguage.UR

    val latestVersionLabel = when (updateState) {
        is UpdateUiState.UpdateAvailable -> "${updateState.update.metadata.versionName} (Build ${updateState.update.metadata.versionCode})"
        is UpdateUiState.Downloading -> "${updateState.update.metadata.versionName} (Build ${updateState.update.metadata.versionCode})"
        is UpdateUiState.Verifying -> "${updateState.update.metadata.versionName} (Build ${updateState.update.metadata.versionCode})"
        is UpdateUiState.PermissionRequired -> "${updateState.update.metadata.versionName} (Build ${updateState.update.metadata.versionCode})"
        is UpdateUiState.ReadyToInstall -> "${updateState.update.metadata.versionName} (Build ${updateState.update.metadata.versionCode})"
        is UpdateUiState.Installing -> "${updateState.update.metadata.versionName} (Build ${updateState.update.metadata.versionCode})"
        is UpdateUiState.UpToDate -> "${updateState.latestVersionName} (Build ${updateState.latestVersionCode})"
        is UpdateUiState.Error -> {
            updateState.update?.let { "${it.metadata.versionName} (Build ${it.metadata.versionCode})" }
                ?: if (isUrdu) "دستیاب نہیں (جانچ ناکام)" else "Unavailable (check failed)"
        }
        is UpdateUiState.Checking -> if (isUrdu) "جانچ جاری ہے..." else "Checking..."
        is UpdateUiState.InstallSucceeded -> updateState.versionName
        UpdateUiState.Idle -> {
            val cachedName = preferences.lastKnownLatestVersionName
            if (!cachedName.isNullOrBlank() && preferences.lastKnownLatestVersionCode > 0) {
                "$cachedName (Build ${preferences.lastKnownLatestVersionCode})"
            } else {
                if (isUrdu) "ابھی تک جانچا نہیں گیا" else "Not checked yet"
            }
        }
    }

    val lastCheckText = if (preferences.lastCheckEpochMs > 0L) {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
            .format(Date(preferences.lastCheckEpochMs))
    } else {
        if (isUrdu) "کبھی نہیں" else "Never checked"
    }

    val (statusText, statusColor) = when (updateState) {
        UpdateUiState.Idle -> (if (isUrdu) "تیار • 24 گھنٹے خودکار جانچ فعال" else "Ready • Auto-checks every 24h") to colors.textSecondary
        UpdateUiState.Checking -> (if (isUrdu) "گٹ ہب ریلیز سے رابطہ جاری ہے..." else "Contacting GitHub Releases...") to colors.cyanPrimary
        is UpdateUiState.UpToDate -> (if (isUrdu) "تازہ ترین ورژن انسٹال ہے" else "Up to date (verified)") to colors.limeSignal
        is UpdateUiState.UpdateAvailable -> (if (isUrdu) "نیا ورژن ${updateState.update.metadata.versionName} دستیاب ہے" else "Update available: v${updateState.update.metadata.versionName}") to colors.cyanPrimary
        is UpdateUiState.Downloading -> (if (isUrdu) "ڈاؤن لوڈ جاری ہے..." else "Downloading update...") to colors.cyanPrimary
        is UpdateUiState.Verifying -> (if (isUrdu) "SHA-256 تصدیق جاری ہے..." else "Verifying SHA-256 & signature...") to colors.cyanPrimary
        is UpdateUiState.PermissionRequired -> (if (isUrdu) "انسٹال اجازت درکار ہے" else "Install permission required") to colors.warnAmber
        is UpdateUiState.ReadyToInstall,
        is UpdateUiState.Installing -> (if (isUrdu) "انسٹال کے لیے تیار" else "Verified & ready to install") to colors.limeSignal
        is UpdateUiState.InstallSucceeded -> (if (isUrdu) "اپ ڈیٹ مکمل" else "Update applied") to colors.limeSignal
        is UpdateUiState.Error -> (if (isUrdu) updateState.messageUr else updateState.message) to colors.alertRed
    }

    val hasActionableUpdate = updateState is UpdateUiState.UpdateAvailable ||
        updateState is UpdateUiState.Downloading ||
        updateState is UpdateUiState.Verifying ||
        updateState is UpdateUiState.PermissionRequired ||
        updateState is UpdateUiState.ReadyToInstall ||
        updateState is UpdateUiState.Installing ||
        (updateState is UpdateUiState.Error && updateState.update != null)

    TechnicalPanel(
        modifier = modifier.testTag("settings_update_panel"),
        borderColor = if (hasActionableUpdate) colors.borderCyan else colors.borderMuted,
        accentRailColor = statusColor,
        showCornerReticles = hasActionableUpdate
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isUrdu) "وائرلیس ایپ اپ ڈیٹس" else "WIRELESS APP UPDATES",
                style = MaterialTheme.typography.labelMedium,
                color = colors.cyanPrimary
            )
            Text(
                text = "GITHUB RELEASES",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary
            )
        }

        Spacer(Modifier.height(12.dp))

        UpdateMetaRow(
            label = if (isUrdu) "انسٹال شدہ ورژن" else "Installed Version",
            value = "$installedVersionName (Build $installedVersionCode)",
            valueColor = colors.textPrimary
        )
        HorizontalDivider(color = colors.borderMuted.copy(alpha = 0.35f), modifier = Modifier.padding(vertical = 8.dp))
        UpdateMetaRow(
            label = if (isUrdu) "تازہ ترین دستیاب ورژن" else "Latest Available Version",
            value = latestVersionLabel,
            valueColor = if (hasActionableUpdate) colors.cyanPrimary else colors.textPrimary
        )
        HorizontalDivider(color = colors.borderMuted.copy(alpha = 0.35f), modifier = Modifier.padding(vertical = 8.dp))
        UpdateMetaRow(
            label = if (isUrdu) "آخری اپ ڈیٹ جانچ" else "Last Update Check",
            value = lastCheckText,
            valueColor = colors.textSecondary
        )
        HorizontalDivider(color = colors.borderMuted.copy(alpha = 0.35f), modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = if (isUrdu) "موجودہ حیثیت" else "Current Status",
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = statusColor,
            modifier = Modifier.testTag("settings_update_status_text")
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onCheckForUpdates,
            enabled = updateState !is UpdateUiState.Checking && updateState !is UpdateUiState.Downloading && updateState !is UpdateUiState.Verifying,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .minimumInteractiveComponentSize()
                .testTag("settings_check_updates_button"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.surfaceDeep,
                contentColor = colors.cyanPrimary
            ),
            border = BorderStroke(1.dp, colors.borderCyan)
        ) {
            if (updateState is UpdateUiState.Checking) {
                CircularProgressIndicator(
                    color = colors.cyanPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
            } else {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = if (isUrdu) "اپ ڈیٹس چیک کریں" else "CHECK FOR UPDATES",
                style = MaterialTheme.typography.labelLarge
            )
        }

        if (hasActionableUpdate) {
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onOpenUpdateDialog,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("settings_open_update_dialog_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.cyanPrimary,
                    contentColor = colors.bgPrimary
                )
            ) {
                Icon(Icons.Filled.SystemUpdate, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isUrdu) "اپ ڈیٹ دیکھیں اور انسٹال کریں" else "VIEW & INSTALL UPDATE",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun UpdateMetaRow(
    label: String,
    value: String,
    valueColor: Color
) {
    val colors = LocalClickwiseColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = valueColor
        )
    }
}

fun formatByteSize(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val kb = 1024.0
    val mb = kb * 1024.0
    return when {
        bytes >= mb -> String.format(Locale.US, "%.1f MB", bytes / mb)
        bytes >= kb -> String.format(Locale.US, "%.0f KB", bytes / kb)
        else -> "$bytes B"
    }
}
