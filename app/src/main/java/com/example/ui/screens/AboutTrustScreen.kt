package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.ClickwiseStrings
import com.example.domain.model.AppLanguage
import com.example.ui.components.ClickwiseBrandWordmark
import com.example.ui.components.SectionEyebrow
import com.example.ui.components.TechnicalPanel
import com.example.ui.components.ZeroRetentionArchitectureDiagram
import com.example.ui.theme.LocalClickwiseColors

@Composable
fun AboutTrustScreen(
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("about_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Brand Identity + Zero-Retention Pipeline Schematic
        item {
            TechnicalPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1040.dp),
                borderColor = colors.borderCyan,
                containerColor = colors.bgSecondary,
                showCornerReticles = true
            ) {
                ClickwiseBrandWordmark(emblemSize = 42.dp, showTagline = true)
                Spacer(Modifier.height(14.dp))
                Text(
                    text = if (language == AppLanguage.UR)
                        "CLICKWISE ایک AI معاون سائبر سیکیورٹی آگاہی پلیٹ فارم ہے جو طلبہ، اساتذہ اور عام صارفین کو مشکوک پیغامات میں خطرے کی علامات پہچاننے اور محفوظ آن لائن عادات اپنانے میں مدد دینے کے لیے بنایا گیا ہے۔"
                    else
                        "CLICKWISE is an AI-assisted cybersecurity awareness platform designed to help students, faculty, and everyday internet users identify potential warning signs in suspicious messages and adopt safer online habits.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary
                )
                Spacer(Modifier.height(16.dp))
                ZeroRetentionArchitectureDiagram(language = language)
            }
        }

        // Mission vs Capabilities Split on Wide Screens
        item {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1040.dp)
            ) {
                val isWide = maxWidth >= 740.dp
                if (isWide) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MissionPanel(
                            language = language,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        LimitsPanel(
                            language = language,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        MissionPanel(language = language, modifier = Modifier.fillMaxWidth())
                        LimitsPanel(language = language, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        // Privacy Principles vs Responsible AI Split on Wide Screens
        item {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1040.dp)
            ) {
                val isWide = maxWidth >= 740.dp
                if (isWide) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PrivacyPanel(
                            language = language,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        ResponsibleAiPanel(
                            language = language,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        PrivacyPanel(language = language, modifier = Modifier.fillMaxWidth())
                        ResponsibleAiPanel(language = language, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun MissionPanel(
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    TechnicalPanel(
        modifier = modifier,
        accentRailColor = colors.cyanPrimary
    ) {
        SectionEyebrow(text = if (language == AppLanguage.UR) "مشن اور بنیادی خصوصیات" else "MISSION & CORE FEATURES")
        Spacer(Modifier.height(10.dp))
        val points = if (language == AppLanguage.UR) {
            listOf(
                "فشنگ، جعلی اسکالرشپ، جعلی ملازمت اور او ٹی پی دھوکہ دہی کی علامات کی وضاحت۔",
                "لنکس کو کھولے یا چلائے بغیر مقامی سطح پر لنک کی ساخت کا معائنہ۔",
                "انگریزی اور اردو (RTL) دونوں زبانوں میں آسان اور عملی آگاہی۔",
                "بغیر اکاؤنٹ اور بغیر پیغام محفوظ کیے سیشن پر مبنی پرائیویسی تحفظ۔"
            )
        } else {
            listOf(
                "Explain warning signs across phishing, fake scholarships, fraudulent internships, and OTP scams.",
                "Inspect URL structures locally without ever opening, fetching, or executing suspicious links.",
                "Deliver accessible bilingual cybersecurity guidance in English and Urdu.",
                "Operate without user accounts or persistent storage of submitted message bodies."
            )
        }
        points.forEach { pt ->
            Row(modifier = Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Filled.Shield, contentDescription = null, tint = colors.cyanPrimary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(pt, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            }
        }
    }
}

@Composable
private fun LimitsPanel(
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    TechnicalPanel(
        modifier = modifier,
        accentRailColor = colors.warnAmber
    ) {
        SectionEyebrow(
            text = if (language == AppLanguage.UR) "تجزیے کی حدود اور شفافیت" else "WHAT CLICKWISE CAN & CANNOT ESTABLISH",
            accentColor = colors.warnAmber
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (language == AppLanguage.UR)
                "• مقامی URL معائنہ کار صرف لنک کی ساخت (مثلاً IP ایڈریس، مختصر لنکس، غیر معمولی سب ڈومینز) کا جائزہ لیتا ہے۔\n• پیغام کا تجزیہ Firebase AI Logic (Gemini Developer API) اور Firebase App Check کے ذریعے محفوظ طریقے سے چلتا ہے، بغیر کسی خام API کلید کے۔\n• یہ ایپ کوئی صارف اکاؤنٹ، Cloud Firestore یا مستقل پیغام اسٹوریج استعمال نہیں کرتی اور نہ ہی کسی بینک یا یونیورسٹی ڈیٹا بیس تک رسائی رکھتی ہے۔"
            else
                "• Local URL inspection evaluates structural patterns only (IP hosts, shorteners, excessive subdomains, suspicious path terms). It does not visit sites or verify live domain reputation.\n• AI message analysis runs via Firebase AI Logic (Gemini Developer API) protected by Firebase App Check without embedding any raw API key in the APK.\n• CLICKWISE uses no user accounts, no Cloud Firestore, and no persistent message storage, and has no access to private banking or university databases.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun PrivacyPanel(
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    TechnicalPanel(
        modifier = modifier,
        borderColor = colors.limeSignal.copy(alpha = 0.38f),
        accentRailColor = colors.limeSignal
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = colors.limeSignal)
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (language == AppLanguage.UR) "رازداری کے اصول" else "PRIVACY PRINCIPLES",
                style = MaterialTheme.typography.titleMedium,
                color = colors.limeSignal
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = ClickwiseStrings.privacyNotice(language),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary
        )
    }
}

@Composable
private fun ResponsibleAiPanel(
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val colors = LocalClickwiseColors.current
    TechnicalPanel(
        modifier = modifier,
        borderColor = colors.warnAmber.copy(alpha = 0.45f),
        accentRailColor = colors.warnAmber
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Info, contentDescription = null, tint = colors.warnAmber)
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (language == AppLanguage.UR) "ذمہ دارانہ استعمال کا انتباہ" else "RESPONSIBLE AI & SECURITY DISCLAIMER",
                style = MaterialTheme.typography.titleMedium,
                color = colors.warnAmber
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = ClickwiseStrings.responsibleAiDisclaimer(language),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary
        )
    }
}
