package com.example.domain.analyzer

import com.example.domain.model.FindingSeverity
import com.example.domain.model.RiskLevel
import com.example.domain.model.UrlInspectionResult
import com.example.domain.model.UrlObservation
import com.example.domain.model.UrlRiskIndicator
import java.net.URI
import java.util.Locale

object UrlInspectorEngine {

    private val shortenerDomains = setOf(
        "bit.ly", "tinyurl.com", "t.co", "goo.gl", "ow.ly", "is.gd", "buff.ly",
        "rb.gy", "cutt.ly", "shorturl.at", "rebrand.ly", "tiny.cc", "bl.ink", "s.id"
    )

    private val suspiciousPathKeywords = listOf(
        "verify", "login", "signin", "sign-in", "otp", "password", "passcode",
        "bank", "account", "suspend", "urgent", "claim", "prize", "reward",
        "scholarship", "stipend", "refund", "kyc", "wallet", "confirm", "update",
        "fee", "payment", "parcel", "customs", "portal-auth"
    )

    private val highRiskTlds = setOf(
        "xyz", "top", "click", "tk", "ml", "ga", "cf", "gq", "buzz", "work", "support", "vip", "loan"
    )

    private val institutionalImpersonationTokens = listOf(
        "hec", "bank", "hbl", "ubl", "meezan", "jazzcash", "easypaisa", "nadra",
        "gov", "edu", "university", "scholarship", "paypal", "amazon", "dhl", "fedex", "tcs"
    )

    private val ipv4Regex = Regex("""^(\d{1,3}\.){3}\d{1,3}$""")
    private val ipv6Regex = Regex("""^\[?[0-9a-fA-F:]+]?$""")
    private val urlExtractRegex = Regex("""(?:https?://|www\.)[^\s<>"{}|\\^`\[\]]+""", RegexOption.IGNORE_CASE)

    fun extractFirstUrl(text: String): String? {
        return urlExtractRegex.find(text)?.value?.trimEnd('.', ',', ';', ')', ']')
    }

    fun inspect(rawInput: String): UrlInspectionResult {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) {
            return invalidResult(rawInput, "Empty URL input", "خالی لنک درج کیا گیا ہے")
        }

        val hadExplicitScheme = trimmed.contains("://")
        val normalized = if (hadExplicitScheme) trimmed else "http://$trimmed"

        val parsedUri = try {
            URI(normalized)
        } catch (_: Exception) {
            return invalidResult(
                trimmed,
                "Malformed URL syntax prevents standard parsing.",
                "لنک کی ساخت غیر معیاری ہے جس سے مکمل تجزیہ ممکن نہیں۔"
            )
        }

        val rawScheme = if (hadExplicitScheme) {
            (parsedUri.scheme ?: "").lowercase(Locale.US)
        } else {
            "none-specified"
        }

        val authority = parsedUri.rawAuthority ?: ""
        val host = (parsedUri.host ?: extractHostFallback(authority)).lowercase(Locale.US)
        if (host.isBlank() || (!host.contains('.') && !ipv4Regex.matches(host) && !host.contains(':'))) {
            return invalidResult(
                trimmed,
                "Could not identify a valid hostname in this input.",
                "اس لنک میں کوئی درست ڈومین نام شناخت نہیں ہو سکا۔"
            )
        }

        val port = if (parsedUri.port != -1) parsedUri.port.toString() else "Default"
        val rawPath = parsedUri.rawPath.orEmpty().ifBlank { "/" }
        val rawQuery = parsedUri.rawQuery.orEmpty()
        val pathAndQuery = if (rawQuery.isNotEmpty()) "$rawPath?$rawQuery" else rawPath

        val hostLabels = host.split('.').filter { it.isNotBlank() }
        val subdomainCount = (hostLabels.size - 2).coerceAtLeast(0)
        val subdomainsDisplay = if (subdomainCount > 0) {
            hostLabels.take(subdomainCount).joinToString(".")
        } else {
            "None"
        }

        val observations = buildList {
            add(UrlObservation("Declared Scheme", "ظاہر کردہ پروٹوکول", rawScheme))
            add(UrlObservation("Hostname", "ہوسٹ نام", host))
            add(UrlObservation("Subdomain Depth", "سب ڈومین سطح", "$subdomainCount ($subdomainsDisplay)"))
            add(UrlObservation("Port", "پورٹ", port))
            add(UrlObservation("Path & Query", "پاتھ اور کوئری", pathAndQuery.take(140)))
        }

        val indicators = mutableListOf<UrlRiskIndicator>()
        var score = 0

        // 1. Non-HTTPS or missing scheme
        when (rawScheme) {
            "http" -> {
                score += 20
                indicators += UrlRiskIndicator(
                    title = "Unencrypted HTTP Scheme",
                    titleUr = "غیر محفوظ HTTP پروٹوکول",
                    severity = FindingSeverity.MEDIUM,
                    observation = "Observed scheme: http://",
                    explanation = "HTTP does not encrypt data in transit. Legitimate login, banking, or scholarship portals use HTTPS (though HTTPS alone never guarantees trustworthiness).",
                    explanationUr = "HTTP ڈیٹا کو انکرپٹ نہیں کرتا۔ مستند پورٹلز HTTPS استعمال کرتے ہیں، تاہم صرف HTTPS کا ہونا کسی ویب سائٹ کے محفوظ ہونے کی ضمانت نہیں ہے۔"
                )
            }
            "none-specified" -> {
                score += 10
                indicators += UrlRiskIndicator(
                    title = "No Scheme Declared",
                    titleUr = "پروٹوکول درج نہیں",
                    severity = FindingSeverity.LOW,
                    observation = "Input lacked http:// or https://",
                    explanation = "The link omitted its transport scheme. Inspect the full copied URL including https:// before trusting it.",
                    explanationUr = "لنک میں پروٹوکول شامل نہیں ہے۔ اعتماد کرنے سے پہلے مکمل لنک چیک کریں۔"
                )
            }
            "javascript", "data", "file" -> {
                score += 45
                indicators += UrlRiskIndicator(
                    title = "Non-Web Executable or Data Scheme",
                    titleUr = "غیر معیاری یا مشکوک اسکیم",
                    severity = FindingSeverity.CRITICAL,
                    observation = "Observed scheme: $rawScheme:",
                    explanation = "Schemes like javascript: or data: can execute scripts or render spoofed content locally instead of opening a real website.",
                    explanationUr = "اس قسم کے لنکس اصل ویب سائٹ کھولنے کے بجائے براؤزر میں مشکوک کوڈ یا جعلی صفحہ چلا سکتے ہیں۔"
                )
            }
        }

        // 2. IP literal host
        val isIpHost = ipv4Regex.matches(host) || (host.contains(':') && ipv6Regex.matches(host))
        if (isIpHost) {
            score += 35
            indicators += UrlRiskIndicator(
                title = "IP Address Used Instead of Domain Name",
                titleUr = "ڈومین نام کے بجائے آئی پی ایڈریس کا استعمال",
                severity = FindingSeverity.HIGH,
                observation = "Observed IP host: $host",
                explanation = "Official universities, banks, and companies almost never ask users to sign in or submit applications through a raw IP address.",
                explanationUr = "مستند یونیورسٹیاں، بینک اور ادارے عموماً صارفین کو براہِ راست آئی پی ایڈریس پر لاگ ان یا فارم بھرنے کا نہیں کہتے۔"
            )
        }

        // 3. @ authority trick
        val hasUserInfo = authority.contains('@') || !parsedUri.rawUserInfo.isNullOrBlank()
        if (hasUserInfo) {
            score += 40
            indicators += UrlRiskIndicator(
                title = "Misleading Authority Syntax (@ Symbol)",
                titleUr = "گمراہ کن '@' علامت کا استعمال",
                severity = FindingSeverity.CRITICAL,
                observation = "Authority contains '@': $authority",
                explanation = "In URLs, text before '@' can look like a trusted domain while the browser actually connects to the host after '@'.",
                explanationUr = "لنک میں '@' سے پہلے نظر آنے والا نام صارف کو دھوکا دینے کے لیے ہو سکتا ہے جبکہ اصل ویب سائٹ '@' کے بعد والی ہوتی ہے۔"
            )
        }

        // 4. Punycode / IDN indicator
        val isPunycode = hostLabels.any { it.startsWith("xn--") }
        if (isPunycode) {
            score += 30
            indicators += UrlRiskIndicator(
                title = "Punycode / Internationalized Domain Indicator",
                titleUr = "Punycode یا مشابہ حروف والا ڈومین",
                severity = FindingSeverity.HIGH,
                observation = "Host contains 'xn--' label: $host",
                explanation = "Punycode domains can be used in homograph attacks where lookalike characters imitate familiar brand or university names.",
                explanationUr = "Punycode ڈومینز بعض اوقات ملتے جلتے حروف استعمال کر کے کسی معروف ادارے کی نقل کرنے کے لیے استعمال ہوتے ہیں۔"
            )
        }

        // 5. URL shortener
        val isShortener = shortenerDomains.any { host == it || host.endsWith(".$it") }
        if (isShortener) {
            score += 25
            indicators += UrlRiskIndicator(
                title = "Known URL Shortener Pattern",
                titleUr = "مختصر لنک (URL Shortener) کا استعمال",
                severity = FindingSeverity.MEDIUM,
                observation = "Shortener host detected: $host",
                explanation = "Shortened links conceal the true destination domain until clicked. Avoid entering credentials or personal data behind shortened URLs.",
                explanationUr = "مختصر لنکس اصل ویب سائٹ کا پتہ چھپا لیتے ہیں۔ ایسے لنکس پر ذاتی یا بینک کی معلومات ہرگز درج نہ کریں۔"
            )
        }

        // 6. Excessive subdomains
        if (!isIpHost && subdomainCount >= 3) {
            score += 22
            indicators += UrlRiskIndicator(
                title = "Excessive Subdomain Nesting",
                titleUr = "غیر معمولی طور پر زیادہ سب ڈومینز",
                severity = FindingSeverity.MEDIUM,
                observation = "$subdomainCount subdomains before root domain: $subdomainsDisplay",
                explanation = "Phishing links often place familiar brand names in left-hand subdomains while the true controlling domain sits at the far right.",
                explanationUr = "فشنگ لنکس میں اکثر معروف اداروں کا نام بائیں طرف سب ڈومین میں رکھا جاتا ہے جبکہ اصل کنٹرولنگ ڈومین آخر میں ہوتا ہے۔"
            )
        }

        // 7. Unusual URL encoding
        val percentMatches = Regex("""%[0-9a-fA-F]{2}""").findAll(trimmed).count()
        if (percentMatches >= 4) {
            score += 18
            indicators += UrlRiskIndicator(
                title = "Heavy Percent-Encoding Detected",
                titleUr = "غیر معمولی فیصد انکوڈنگ کی موجودگی",
                severity = FindingSeverity.MEDIUM,
                observation = "$percentMatches encoded byte sequences in URL",
                explanation = "Excessive percent-encoding can obscure keywords, redirects, or misleading parameters from casual inspection.",
                explanationUr = "زیادہ انکوڈنگ لنک کے اندر موجود الفاظ یا ری ڈائریکٹس کو چھپانے کے لیے استعمال ہو سکتی ہے۔"
            )
        }

        // 8. Suspicious path/query keywords
        val lowerPathQuery = pathAndQuery.lowercase(Locale.US)
        val matchedKeywords = suspiciousPathKeywords.filter { lowerPathQuery.contains(it) }
        if (matchedKeywords.isNotEmpty()) {
            val kwBoost = (matchedKeywords.size * 7).coerceAtMost(24)
            score += kwBoost
            indicators += UrlRiskIndicator(
                title = "Sensitive Action Keywords in Path/Query",
                titleUr = "لنک کے پاتھ میں حساس الفاظ",
                severity = if (matchedKeywords.size >= 2) FindingSeverity.HIGH else FindingSeverity.MEDIUM,
                observation = "Observed keywords: ${matchedKeywords.joinToString(", ")}",
                explanation = "Keywords related to verification, OTPs, scholarships, or account claims in an unsolicited link warrant independent verification.",
                explanationUr = "تصدیق، او ٹی پی، اسکالرشپ یا انعام جیسے الفاظ پر مشتمل غیر متوقع لنکس کی آزادانہ تصدیق ضروری ہے۔"
            )
        }

        // 9. Potentially misleading hyphenated/lookalike domain or high-risk TLD
        val rootDomain = if (hostLabels.size >= 2) hostLabels.takeLast(2).joinToString(".") else host
        val tld = hostLabels.lastOrNull().orEmpty()
        val hyphenCount = host.count { it == '-' }
        val matchedBrands = institutionalImpersonationTokens.filter { token ->
            host.contains(token) && !isOfficialTypicalDomain(rootDomain, token)
        }

        if (matchedBrands.isNotEmpty() || hyphenCount >= 2) {
            score += 22
            val obs = buildString {
                if (matchedBrands.isNotEmpty()) append("Contains institutional keywords (${matchedBrands.joinToString(", ")}) in '$rootDomain'")
                if (matchedBrands.isNotEmpty() && hyphenCount >= 2) append("; ")
                if (hyphenCount >= 2) append("$hyphenCount hyphens in hostname")
            }
            indicators += UrlRiskIndicator(
                title = "Potentially Misleading Domain Structure",
                titleUr = "ممکنہ طور پر گمراہ کن ڈومین نام",
                severity = FindingSeverity.HIGH,
                observation = obs,
                explanation = "Attackers frequently combine official-sounding keywords with hyphens or unrelated TLDs to mimic trusted institutions.",
                explanationUr = "حملہ آور اکثر معروف اداروں کے ناموں کے ساتھ ڈیش یا غیر متعلقہ ڈومین ملا کر جعلی ویب سائٹس بناتے ہیں۔"
            )
        }

        if (tld in highRiskTlds) {
            score += 15
            indicators += UrlRiskIndicator(
                title = "Uncommon Promotional Top-Level Domain",
                titleUr = "غیر روایتی ٹاپ لیول ڈومین (.$tld)",
                severity = FindingSeverity.LOW,
                observation = "Observed TLD: .$tld",
                explanation = "While any TLD can host legitimate sites, low-cost promotional TLDs are commonly abused in disposable phishing campaigns.",
                explanationUr = "اگرچہ ہر ڈومین پر درست سائٹس ہو سکتی ہیں، لیکن .$tld جیسے سستے ڈومینز عارضی فشنگ مہمات میں زیادہ استعمال ہوتے ہیں۔"
            )
        }

        val clampedScore = score.coerceIn(0, 100)
        return UrlInspectionResult(
            rawUrl = trimmed,
            isValidFormat = true,
            scheme = rawScheme,
            host = host,
            port = port,
            subdomains = subdomainsDisplay,
            pathAndQuery = pathAndQuery,
            observations = observations,
            riskIndicators = indicators,
            patternRiskScore = clampedScore,
            patternRiskLevel = RiskLevel.fromScore(clampedScore)
        )
    }

    private fun isOfficialTypicalDomain(rootDomain: String, token: String): Boolean {
        return rootDomain.endsWith(".edu.pk") ||
            rootDomain.endsWith(".gov.pk") ||
            rootDomain == "$token.com" ||
            rootDomain == "$token.edu" ||
            rootDomain == "$token.org"
    }

    private fun extractHostFallback(authority: String): String {
        val afterAt = authority.substringAfterLast('@')
        return afterAt.substringBefore(':')
    }

    private fun invalidResult(raw: String, reasonEn: String, reasonUr: String): UrlInspectionResult {
        return UrlInspectionResult(
            rawUrl = raw,
            isValidFormat = false,
            scheme = "invalid",
            host = "unparsed",
            port = "N/A",
            subdomains = "N/A",
            pathAndQuery = "",
            observations = listOf(UrlObservation("Parse Status", "تجزیہ کی حیثیت", reasonEn)),
            riskIndicators = listOf(
                UrlRiskIndicator(
                    title = "Unparseable URL Format",
                    titleUr = "ناقابلِ شناخت لنک ساخت",
                    severity = FindingSeverity.MEDIUM,
                    observation = raw.take(80),
                    explanation = reasonEn,
                    explanationUr = reasonUr
                )
            ),
            patternRiskScore = 30,
            patternRiskLevel = RiskLevel.MEDIUM
        )
    }
}
