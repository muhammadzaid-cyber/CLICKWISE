package com.example.data

import com.example.domain.model.CyberLesson
import com.example.domain.model.LessonQuestion
import com.example.domain.model.QuizQuestion
import com.example.domain.model.SampleScenario

object EducationalContentRepository {

    val sampleScenarios: List<SampleScenario> = listOf(
        SampleScenario(
            id = "sample_scholarship",
            category = "Fictional Test Case • Scholarship",
            categoryUr = "فرضی تعلیمی مثال • اسکالرشپ",
            title = "Fake University Merit Scholarship",
            titleUr = "جعلی یونیورسٹی میرٹ اسکالرشپ",
            message = "CONGRATULATIONS! You have been selected for the National Merit Laptop & PKR 85,000 Scholarship Grant. Your slot expires tonight (within 24 hours). Submit your CNIC, bank account number, and OTP verification code at http://hec-scholarship-claim.xyz/portal-auth/verify to release your stipend immediately.",
            messageUr = "مبارک ہو! آپ کو نیشنل میرٹ لیپ ٹاپ اور 85,000 روپے اسکالرشپ گرانٹ کے لیے منتخب کیا گیا ہے۔ آپ کی نشست آج رات (24 گھنٹے میں) ختم ہو جائے گی۔ وظیفہ فوری حاصل کرنے کے لیے http://hec-scholarship-claim.xyz/portal-auth/verify پر اپنا شناختی کارڈ، بینک اکاؤنٹ نمبر اور او ٹی پی درج کریں۔",
            sampleUrl = "http://hec-scholarship-claim.xyz/portal-auth/verify"
        ),
        SampleScenario(
            id = "sample_internship",
            category = "Fictional Test Case • Internship",
            categoryUr = "فرضی تعلیمی مثال • انٹرن شپ",
            title = "Fraudulent Remote Internship Selection",
            titleUr = "جعلی ریموٹ انٹرن شپ سلیکشن",
            message = "Dear Student, HR Department has approved your remote internship offer letter with PKR 65,000 monthly stipend and no interview required. To activate your company portal and receive your onboarding badge, transfer a refundable registration fee of PKR 2,500 via JazzCash/EasyPaisa within 2 hours or your slot will be forfeited.",
            messageUr = "عزیز طالب علم، ایچ آر ڈیپارٹمنٹ نے بغیر انٹرویو کے 65,000 روپے ماہانہ وظیفے کے ساتھ آپ کی ریموٹ انٹرن شپ منظور کر لی ہے۔ پورٹل فعال کرنے کے لیے 2 گھنٹوں کے اندر 2,500 روپے قابلِ واپسی رجسٹریشن فیس جیز کیش/ایزی پیسہ کے ذریعے جمع کرائیں ورنہ آپ کی نشست منسوخ ہو جائے گی۔",
            sampleUrl = "https://bit.ly/3xFakeInternOffer"
        ),
        SampleScenario(
            id = "sample_parcel",
            category = "Fictional Test Case • Delivery",
            categoryUr = "فرضی تعلیمی مثال • پارسل",
            title = "Fake Parcel Customs Fee Request",
            titleUr = "جعلی پارسل کسٹم فیس کا پیغام",
            message = "Courier Alert: Your international parcel delivery failed due to an incomplete address and unpaid customs clearance fee (PKR 480). Confirm your home address and debit card details immediately at http://198.51.100.42/redelivery/confirm to avoid shipment return.",
            messageUr = "کوریئر الرٹ: نامکمل پتے اور 480 روپے کسٹم فیس کی عدم ادائیگی کے باعث آپ کا پارسل روک لیا گیا ہے۔ واپسی سے بچنے کے لیے فوری طور پر http://198.51.100.42/redelivery/confirm پر اپنا پتہ اور ڈیبٹ کارڈ تفصیلات درج کریں۔",
            sampleUrl = "http://198.51.100.42/redelivery/confirm"
        ),
        SampleScenario(
            id = "sample_bank",
            category = "Fictional Test Case • Banking",
            categoryUr = "فرضی تعلیمی مثال • بینکنگ",
            title = "Fake Bank KYC Suspension Alert",
            titleUr = "جعلی بینک اکاؤنٹ معطلی الرٹ",
            message = "URGENT SECURITY NOTICE: Your bank account will be permanently blocked today due to incomplete KYC verification. Update your ATM card PIN, IBAN, and 6-digit OTP code immediately using our secure link: https://secure-bank-kyc-update.top/login",
            messageUr = "فوری سیکیورٹی نوٹس: نامکمل کے وائی سی تصدیق کی وجہ سے آپ کا بینک اکاؤنٹ آج مستقل بند کر دیا جائے گا۔ فوری بحالی کے لیے https://secure-bank-kyc-update.top/login پر اپنا اے ٹی ایم پن، آئی بی این اور 6 ہندسوں کا او ٹی پی درج کریں۔",
            sampleUrl = "https://secure-bank-kyc-update.top/login"
        ),
        SampleScenario(
            id = "sample_prize",
            category = "Fictional Test Case • Social Media",
            categoryUr = "فرضی تعلیمی مثال • سوشل میڈیا",
            title = "Suspicious Social Media Prize Claim",
            titleUr = "مشکوک سوشل میڈیا انعام کا اعلان",
            message = "Winner Notification! Your mobile number won the Anniversary Lucky Draw smartphone prize. Claim your unclaimed reward now before last chance expires! Click http://portal@reward-claim-center.click/claim and enter your wallet password to cover processing.",
            messageUr = "جیتنے والے صارف! آپ کے نمبر نے اینیورسری لکی ڈرا میں اسمارٹ فون انعام جیت لیا ہے۔ آخری موقع ختم ہونے سے پہلے http://portal@reward-claim-center.click/claim پر کلک کریں اور پروسیسنگ کے لیے اپنا والٹ پاس ورڈ درج کریں۔",
            sampleUrl = "http://portal@reward-claim-center.click/claim"
        )
    )

    val lessons: List<CyberLesson> = listOf(
        CyberLesson(
            id = "lesson_phishing",
            number = 1,
            title = "What is Phishing?",
            titleUr = "فشنگ (Phishing) کیا ہے؟",
            intro = "Phishing is a deceptive technique where attackers disguise fraudulent messages as trusted institutions to steal credentials, OTPs, or money.",
            introUr = "فشنگ ایک دھوکہ دہی کا طریقہ ہے جس میں حملہ آور خود کو کسی معتبر ادارے کے طور پر پیش کر کے آپ کے پاس ورڈ، او ٹی پی یا رقم چرانے کی کوشش کرتے ہیں۔",
            explanation = "Attackers send emails, SMS, or WhatsApp messages pretending to be your university, bank, or courier service. Their goal is to create urgency or excitement so you click a lookalike link and submit private information on a fake webpage.",
            explanationUr = "حملہ آور ای میل، ایس ایم ایس یا واٹس ایپ کے ذریعے یونیورسٹی، بینک یا کوریئر کمپنی بن کر پیغامات بھیجتے ہیں۔ ان کا مقصد آپ کو جلدی یا لالچ میں ڈال کر جعلی لنک پر ذاتی معلومات درج کروانا ہوتا ہے۔",
            illustrativeExample = "Example: An SMS claiming 'Your university portal password expires in 2 hours — sign in at portal-login-help.xyz to keep access.'",
            illustrativeExampleUr = "مثال: ایک پیغام جس میں لکھا ہو 'آپ کا یونیورسٹی پورٹل پاس ورڈ 2 گھنٹے میں ختم ہو رہا ہے — رسائی برقرار رکھنے کے لیے portal-login-help.xyz پر لاگ ان کریں۔'",
            warningSigns = listOf(
                "Generic greetings instead of your verified name or student ID.",
                "Mismatched sender domains that imitate official organizations.",
                "Direct links to login or credential reset pages with urgent deadlines."
            ),
            warningSignsUr = listOf(
                "آپ کے اصل نام یا اسٹوڈنٹ آئی ڈی کے بجائے عمومی القابات کا استعمال۔",
                "سرکاری اداروں سے ملتے جلتے مگر جعلی ڈومین نام۔",
                "فوری آخری تاریخ کے ساتھ لاگ ان یا پاس ورڈ تبدیل کرنے کے لنکس۔"
            ),
            protectionTips = listOf(
                "Never log in via a link inside an unexpected message.",
                "Type the official website address manually or use saved bookmarks.",
                "Verify suspicious alerts directly with your university IT desk or bank helpline."
            ),
            protectionTipsUr = listOf(
                "غیر متوقع پیغام میں موجود لنک کے ذریعے کبھی لاگ ان نہ کریں۔",
                "ہمیشہ سرکاری ویب سائٹ کا پتہ خود ٹائپ کریں یا محفوظ بک مارک استعمال کریں۔",
                "مشکوک الرٹ کی تصدیق براہِ راست متعلقہ ادارے کی ہیلپ لائن سے کریں۔"
            ),
            knowledgeCheck = LessonQuestion(
                prompt = "What is the safest action if a text says your account will be closed unless you click a login link?",
                promptUr = "اگر کسی پیغام میں لکھا ہو کہ لنک پر کلک نہ کرنے پر آپ کا اکاؤنٹ بند ہو جائے گا، تو سب سے محفوظ عمل کیا ہے؟",
                options = listOf(
                    "Click the link quickly to prevent suspension",
                    "Open the official app or website independently to check",
                    "Reply with your student ID and password",
                    "Forward the link to friends to test it"
                ),
                optionsUr = listOf(
                    "بندش سے بچنے کے لیے فوراً لنک پر کلک کریں",
                    "الگ سے سرکاری ایپ یا ویب سائٹ کھول کر خود تصدیق کریں",
                    "جواب میں اپنا آئی ڈی اور پاس ورڈ بھیج دیں",
                    "دوستوں کو فارورڈ کر کے چیک کروائیں"
                ),
                correctIndex = 1,
                explanation = "Always bypass the message link and check your account directly through the official website or app.",
                explanationUr = "ہمیشہ پیغام کے لنک کو نظرانداز کریں اور براہِ راست سرکاری ایپ یا ویب سائٹ سے تصدیق کریں۔"
            )
        ),
        CyberLesson(
            id = "lesson_social_engineering",
            number = 2,
            title = "How Social Engineering Works",
            titleUr = "سوشل انجینئرنگ کیسے کام کرتی ہے؟",
            intro = "Social engineering manipulates human emotions—fear, urgency, curiosity, or trust—rather than hacking software directly.",
            introUr = "سوشل انجینئرنگ سافٹ ویئر کو ہیک کرنے کے بجائے انسانی جذبات یعنی خوف، عجلت، تجسس یا اعتماد کا غلط فائدہ اٹھاتی ہے۔",
            explanation = "Scammers research common student needs like scholarships, exam results, or part-time income. By posing as helpful officials or frightened colleagues, they persuade victims to hand over access voluntarily.",
            explanationUr = "دھوکہ باز طلبہ کی ضروریات جیسے اسکالرشپس، امتحانی نتائج یا جزوقتی آمدنی کا فائدہ اٹھاتے ہیں۔ وہ خود کو مددگار افسر یا ساتھی ظاہر کر کے صارفین کو ذاتی معلومات دینے پر آمادہ کرتے ہیں۔",
            illustrativeExample = "Example: A caller pretending to be university IT support asks for your verification code to 'fix a fee challan sync error.'",
            illustrativeExampleUr = "مثال: یونیورسٹی آئی ٹی سپورٹ بن کر کال کرنے والا شخص 'فیس چالان کا مسئلہ حل کرنے' کے بہانے آپ سے تصدیقی کوڈ مانگتا ہے۔",
            warningSigns = listOf(
                "Strong emotional pressure: panic, threats, or 'once-in-a-lifetime' excitement.",
                "Requests to bypass normal verification steps or keep things secret.",
                "Authority claims paired with personal WhatsApp numbers or Gmail addresses."
            ),
            warningSignsUr = listOf(
                "شدید جذباتی دباؤ: خوف، دھمکی یا غیر معمولی خوشخبری کا لالچ۔",
                "معمول کے تصدیقی طریقہ کار کو نظرانداز کرنے یا بات خفیہ رکھنے کا اصرار۔",
                "بڑے عہدے کے دعوے کے ساتھ ذاتی واٹس ایپ نمبر یا غیر سرکاری ای میل کا استعمال۔"
            ),
            protectionTips = listOf(
                "Pause for 60 seconds whenever a message demands immediate action.",
                "Verify authority claims using an independent, publicly listed contact number.",
                "Remember: real IT staff and banks never need your secret codes."
            ),
            protectionTipsUr = listOf(
                "جب بھی کوئی پیغام فوری کارروائی کا مطالبہ کرے تو کم از کم ایک منٹ رک کر سوچیں۔",
                "عہدے کے کسی بھی دعوے کی تصدیق سرکاری طور پر درج نمبر سے کریں۔",
                "یاد رکھیں: اصل آئی ٹی عملہ اور بینک کبھی آپ کے خفیہ کوڈ نہیں مانگتے۔"
            ),
            knowledgeCheck = LessonQuestion(
                prompt = "Why do social engineers create artificial urgency?",
                promptUr = "سوشل انجینئرز مصنوعی عجلت یا گھبراہٹ کیوں پیدا کرتے ہیں؟",
                options = listOf(
                    "Because servers delete accounts every hour",
                    "To rush you so you skip verifying the request",
                    "To improve internet speed during login",
                    "Because official policies forbid waiting"
                ),
                optionsUr = listOf(
                    "کیونکہ سرور ہر گھنٹے اکاؤنٹ حذف کر دیتے ہیں",
                    "تاکہ آپ گھبراہٹ میں تصدیق کیے بغیر غلط قدم اٹھا لیں",
                    "لاگ ان کی رفتار تیز کرنے کے لیے",
                    "کیونکہ سرکاری قوانین انتظار کی اجازت نہیں دیتے"
                ),
                correctIndex = 1,
                explanation = "Artificial deadlines prevent victims from pausing to verify the claim with official sources.",
                explanationUr = "مصنوعی آخری تاریخ اس لیے دی جاتی ہے تاکہ آپ رک کر سرکاری ذرائع سے تصدیق نہ کر سکیں۔"
            )
        ),
        CyberLesson(
            id = "lesson_scholarships",
            number = 3,
            title = "Identifying Fake Scholarship Offers",
            titleUr = "جعلی اسکالرشپ پیغامات کی پہچان",
            intro = "Students are frequently targeted with fabricated laptop, stipend, and merit scholarship announcements on social media and messaging apps.",
            introUr = "طلبہ کو اکثر سوشل میڈیا اور میسجنگ ایپس پر لیپ ٹاپ، وظیفے اور میرٹ اسکالرشپ کے جعلی اعلانات کے ذریعے نشانہ بنایا جاتا ہے۔",
            explanation = "Fraudulent scholarship posts copy official logos and promise guaranteed grants without an application or formal departmental review. Soon after, they ask for registration fees, CNIC copies, or banking credentials.",
            explanationUr = "جعلی اسکالرشپ پوسٹس میں سرکاری لوگو استعمال کر کے بغیر کسی باضابطہ درخواست کے گرانٹ کا وعدہ کیا جاتا ہے، اور پھر رجسٹریشن فیس یا بینک تفصیلات مانگی جاتی ہیں۔",
            illustrativeExample = "Example: A forwarded WhatsApp message promising a PKR 75,000 stipend if you pay a PKR 1,500 'verification fee' via mobile wallet.",
            illustrativeExampleUr = "مثال: واٹس ایپ پر فارورڈ شدہ پیغام جس میں 75,000 روپے وظیفے کے لیے 1,500 روپے 'تصدیقی فیس' موبائل والٹ میں جمع کرانے کا کہا جائے۔",
            warningSigns = listOf(
                "You never applied for the scholarship or merit program.",
                "Requests for upfront processing, card printing, or clearance fees.",
                "Unrecognized third-party domains instead of .edu.pk or .gov.pk portals."
            ),
            warningSignsUr = listOf(
                "آپ نے اس اسکالرشپ پروگرام کے لیے کبھی درخواست ہی نہیں دی۔",
                "پروسیسنگ، کارڈ پرنٹنگ یا کلیئرنس فیس پیشگی طلب کرنا۔",
                "سرکاری پورٹلز کے بجائے غیر معروف ویب سائٹ لنکس کا استعمال۔"
            ),
            protectionTips = listOf(
                "Verify every scholarship on your university's official financial aid office noticeboard or website.",
                "Never pay money to receive a genuine merit scholarship.",
                "Do not upload CNIC photos or bank details on unverified forms."
            ),
            protectionTipsUr = listOf(
                "ہر اسکالرشپ کی تصدیق اپنی یونیورسٹی کے فنانشل ایڈ دفتر یا سرکاری ویب سائٹ سے کریں۔",
                "اصلی میرٹ اسکالرشپ حاصل کرنے کے لیے کبھی پیشگی رقم ادا نہیں کرنی پڑتی۔",
                "غیر مصدقہ فارمز پر شناختی کارڈ یا بینک تفصیلات اپ لوڈ نہ کریں۔"
            ),
            knowledgeCheck = LessonQuestion(
                prompt = "Which sign strongly indicates a scholarship message is fraudulent?",
                promptUr = "ان میں سے کون سی علامت جعلی اسکالرشپ پیغام کی واضح نشانی ہے؟",
                options = listOf(
                    "It appears on your university's official .edu portal",
                    "It requires a small upfront wallet transfer to unlock the grant",
                    "It asks you to visit the campus financial aid office",
                    "It lists an official academic eligibility criteria"
                ),
                optionsUr = listOf(
                    "یہ آپ کی یونیورسٹی کے سرکاری پورٹل پر شائع ہو",
                    "گرانٹ حاصل کرنے کے لیے موبائل والٹ میں پیشگی فیس مانگی جائے",
                    "کیمپس کے فنانشل ایڈ آفس آنے کا کہا جائے",
                    "اس میں باضابطہ تعلیمی اہلیت درج ہو"
                ),
                correctIndex = 1,
                explanation = "Legitimate merit scholarships do not require upfront personal wallet transfers to release funds.",
                explanationUr = "مستند میرٹ اسکالرشپس رقم جاری کرنے کے لیے پیشگی ذاتی ادائیگی نہیں مانگتیں۔"
            )
        ),
        CyberLesson(
            id = "lesson_jobs",
            number = 4,
            title = "Recognizing Fraudulent Job Offers",
            titleUr = "جعلی ملازمت اور انٹرن شپ کی پہچان",
            intro = "Fake remote internships and high-paying part-time jobs prey on students seeking experience or extra income.",
            introUr = "جعلی ریموٹ انٹرن شپس اور زیادہ تنخواہ والی جزوقتی نوکریاں تجربہ یا آمدنی کے خواہشمند طلبہ کو نشانہ بناتی ہیں۔",
            explanation = "Scammers send instant offer letters with zero interviews, inflated daily payouts, or 'task-based' earning schemes. Eventually, they demand security deposits, training fees, or sensitive identity documents.",
            explanationUr = "دھوکہ باز بغیر انٹرویو کے فوری آفر لیٹر، غیر معمولی یومیہ آمدنی یا ٹاسک پر مبنی کمائی کا جھانسہ دیتے ہیں اور بعد میں سیکیورٹی ڈپازٹ یا ٹریننگ فیس طلب کرتے ہیں۔",
            illustrativeExample = "Example: 'Earn PKR 5,000 daily liking videos — deposit PKR 3,000 to unlock Tier-2 VIP tasks.'",
            illustrativeExampleUr = "مثال: 'ویڈیوز لائک کر کے روزانہ 5,000 روپے کمائیں — اگلا لیول کھولنے کے لیے 3,000 روپے جمع کرائیں۔'",
            warningSigns = listOf(
                "Instant hiring with no interview or technical screening.",
                "Requests for onboarding fees, uniform charges, or 'refundable deposits'.",
                "Recruiters operating solely via anonymous messaging numbers."
            ),
            warningSignsUr = listOf(
                "بغیر کسی انٹرویو یا اسکریننگ کے فوری ملازمت کی پیشکش۔",
                "آن بورڈنگ فیس، ٹریننگ چارجز یا 'قابلِ واپسی ڈپازٹ' کا مطالبہ۔",
                "کمپنی کے سرکاری ای میل کے بجائے صرف نامعلوم نمبرز سے رابطہ۔"
            ),
            protectionTips = listOf(
                "Verify open roles directly on the employer's official Careers page.",
                "Never pay a fee to be hired for a job or internship.",
                "Be wary of 'pay-to-unlock-earnings' task schemes."
            ),
            protectionTipsUr = listOf(
                "ملازمت کی تصدیق براہِ راست کمپنی کے سرکاری کیریئرز صفحے پر کریں۔",
                "نوکری یا انٹرن شپ حاصل کرنے کے لیے کبھی کوئی فیس ادا نہ کریں۔",
                "پیسے جمع کرا کے کمائی بڑھانے والی اسکیموں سے دور رہیں۔"
            ),
            knowledgeCheck = LessonQuestion(
                prompt = "A recruiter sends an instant internship offer with no interview and asks for PKR 2,000 for an ID card. What should you do?",
                promptUr = "اگر کوئی بھرتی کنندہ بغیر انٹرویو انٹرن شپ آفر بھیجے اور شناختی کارڈ کے لیے 2,000 روپے مانگے تو کیا کریں؟",
                options = listOf(
                    "Pay immediately since it is refundable",
                    "Refuse payment and verify with the real company directly",
                    "Send your bank ATM PIN instead",
                    "Share your friends' phone numbers"
                ),
                optionsUr = listOf(
                    "فوری ادائیگی کریں کیونکہ یہ قابلِ واپسی ہے",
                    "ادائیگی سے انکار کریں اور کمپنی سے براہِ راست تصدیق کریں",
                    "اے ٹی ایم پن بھیج دیں",
                    "دوستوں کے نمبر شیئر کریں"
                ),
                correctIndex = 1,
                explanation = "Legitimate employers do not charge students upfront onboarding or badge fees via chat.",
                explanationUr = "مستند کمپنیاں چیٹ کے ذریعے طلبہ سے آن بورڈنگ یا کارڈ کی پیشگی فیس وصول نہیں کرتیں۔"
            )
        ),
        CyberLesson(
            id = "lesson_otp",
            number = 5,
            title = "Why OTPs Must Remain Private",
            titleUr = "او ٹی پی (OTP) خفیہ رکھنا کیوں ضروری ہے؟",
            intro = "A One-Time Password (OTP) is a single-use cryptographic key protecting your bank, wallet, and social accounts.",
            introUr = "ون ٹائم پاس ورڈ (OTP) ایک خفیہ چابی ہے جو آپ کے بینک، موبائل والٹ اور سوشل اکاؤنٹس کی حفاظت کرتی ہے۔",
            explanation = "Whenever an attacker already knows or guesses your phone number or password, the OTP is the final lock stopping them from draining funds or hijacking your account. If you read or enter that code for someone else, you hand them full control.",
            explanationUr = "اگر حملہ آور کے پاس آپ کا نمبر یا پاس ورڈ آ بھی جائے، تو او ٹی پی وہ آخری تالا ہے جو اکاؤنٹ کو بچاتا ہے۔ کسی کو یہ کوڈ بتانا اپنے اکاؤنٹ کا کنٹرول اس کے حوالے کرنے کے برابر ہے۔",
            illustrativeExample = "Example: Someone calls claiming they sent a prize code to your SMS by mistake and asks you to read back the 6-digit number.",
            illustrativeExampleUr = "مثال: کوئی کال کر کے کہے کہ غلطی سے آپ کے نمبر پر کوڈ آ گیا ہے، براہِ کرم 6 ہندسوں کا نمبر بتا دیں۔",
            warningSigns = listOf(
                "Anyone asking you to read, forward, or screenshot an OTP.",
                "SMS messages stating 'Do not share this code' followed by someone asking for it.",
                "Forms requesting OTPs for 'verification' of prizes or deliveries."
            ),
            warningSignsUr = listOf(
                "کوئی بھی شخص جو آپ سے او ٹی پی پڑھنے، فارورڈ کرنے یا اسکرین شاٹ مانگے۔",
                "ایس ایم ایس میں کوڈ شیئر نہ کرنے کی تنبیہ کے باوجود کسی کا وہی کوڈ مانگنا۔",
                "انعام یا پارسل کی تصدیق کے نام پر او ٹی پی مانگنے والے فارمز۔"
            ),
            protectionTips = listOf(
                "Treat OTPs like your ATM PIN — never share them with callers, friends, or support agents.",
                "Read the full OTP SMS carefully to see what transaction or login it authorizes.",
                "Enable app-based two-factor authentication where supported."
            ),
            protectionTipsUr = listOf(
                "او ٹی پی کو اپنے اے ٹی ایم پن کی طرح خفیہ رکھیں، کسی کالر یا سپورٹ ایجنٹ کو نہ بتائیں۔",
                "او ٹی پی کا پورا پیغام غور سے پڑھیں کہ وہ کس لاگ ان یا لین دین کے لیے ہے۔",
                "جہاں ممکن ہو وہاں ایپ پر مبنی ٹو فیکٹر آتھنٹیکیشن فعال کریں۔"
            ),
            knowledgeCheck = LessonQuestion(
                prompt = "Can a real bank representative ask for your OTP over the phone to cancel a fraudulent transaction?",
                promptUr = "کیا بینک کا اصل نمائندہ مشکوک ٹرانزیکشن منسوخ کرنے کے لیے فون پر آپ سے او ٹی پی مانگ سکتا ہے؟",
                options = listOf(
                    "Yes, if they sound polite and urgent",
                    "No — bank staff never need your OTP",
                    "Only on weekends",
                    "Yes, if they already know your name"
                ),
                optionsUr = listOf(
                    "جی ہاں، اگر وہ شائستگی اور جلدی سے بات کریں",
                    "نہیں — بینک عملے کو کبھی آپ کے او ٹی پی کی ضرورت نہیں ہوتی",
                    "صرف ہفتے کے آخر میں",
                    "جی ہاں، اگر انہیں آپ کا نام معلوم ہو"
                ),
                correctIndex = 1,
                explanation = "Legitimate bank staff never ask for OTPs or PINs; attackers use your name to build false credibility.",
                explanationUr = "اصلی بینک عملہ کبھی او ٹی پی یا پن نہیں مانگتا؛ دھوکہ باز اعتماد جیتنے کے لیے آپ کا نام استعمال کرتے ہیں۔"
            )
        ),
        CyberLesson(
            id = "lesson_urls",
            number = 6,
            title = "How Suspicious URLs Work",
            titleUr = "مشکوک لنکس (URLs) کیسے دھوکا دیتے ہیں؟",
            intro = "Understanding how a web address is structured lets you spot deceptive domains before tapping.",
            introUr = "ویب ایڈریس کی ساخت کو سمجھ کر آپ کلک کرنے سے پہلے ہی جعلی اور گمراہ کن ڈومینز کو پہچان سکتے ہیں۔",
            explanation = "In a URL like 'https://bank-login.example.xyz/verify', the real website owner is the root domain right before the first single slash ('example.xyz'), NOT 'bank-login'. Attackers also use raw IP addresses, URL shorteners, punycode lookalikes, and '@' symbols to disguise destinations.",
            explanationUr = "لنک 'https://bank-login.example.xyz/verify' میں اصل مالک 'example.xyz' ہے، نہ کہ 'bank-login'۔ حملہ آور آئی پی ایڈریس، مختصر لنکس اور '@' علامت بھی منزل چھپانے کے لیے استعمال کرتے ہیں۔",
            illustrativeExample = "Example: 'https://university.edu.pk.account-verify.top/login' — the true root domain is 'account-verify.top', not the university.",
            illustrativeExampleUr = "مثال: 'https://university.edu.pk.account-verify.top/login' میں اصل ڈومین 'account-verify.top' ہے، یونیورسٹی نہیں۔",
            warningSigns = listOf(
                "Trusted brand names placed far to the left as subdomains.",
                "Raw IP addresses (e.g., http://198.51.100.42/login) instead of domain names.",
                "Shortened links or '@' authority tricks hiding the actual host."
            ),
            warningSignsUr = listOf(
                "معروف ادارے کا نام بائیں طرف سب ڈومین میں رکھنا۔",
                "ڈومین نام کے بجائے براہِ راست آئی پی ایڈریس کا استعمال۔",
                "مختصر لنکس یا '@' علامت کے ذریعے اصل ہوسٹ کو چھپانا۔"
            ),
            protectionTips = listOf(
                "Read the root domain immediately before the first '/' carefully.",
                "Inspect unfamiliar links with CLICKWISE URL Inspector before opening.",
                "Remember: HTTPS padlock only means traffic is encrypted, NOT that the site is honest."
            ),
            protectionTipsUr = listOf(
                "پہلے '/' سے عین پہلے والا اصل ڈومین غور سے پڑھیں۔",
                "مشکوک لنک کھولنے سے پہلے CLICKWISE URL Inspector سے جانچیں۔",
                "یاد رکھیں: HTTPS تالا صرف ڈیٹا انکرپشن ظاہر کرتا ہے، ویب سائٹ کے دیانتدار ہونے کی ضمانت نہیں دیتا۔"
            ),
            knowledgeCheck = LessonQuestion(
                prompt = "In 'https://mybank.portal-security-check.xyz/login', which domain actually controls the website?",
                promptUr = "'https://mybank.portal-security-check.xyz/login' میں ویب سائٹ کا اصل کنٹرول کس ڈومین کے پاس ہے؟",
                options = listOf(
                    "mybank",
                    "portal-security-check.xyz",
                    "login",
                    "https"
                ),
                optionsUr = listOf(
                    "mybank",
                    "portal-security-check.xyz",
                    "login",
                    "https"
                ),
                correctIndex = 1,
                explanation = "The domain immediately before the first single '/' ('portal-security-check.xyz') controls the destination.",
                explanationUr = "پہلے '/' سے عین پہلے والا ڈومین ('portal-security-check.xyz') ہی اصل ویب سائٹ کو کنٹرول کرتا ہے۔"
            )
        ),
        CyberLesson(
            id = "lesson_passwords",
            number = 7,
            title = "Password Security Fundamentals",
            titleUr = "مضبوط پاس ورڈ کی بنیادیں",
            intro = "Reusing weak passwords across student portals, email, and banking makes one leak compromise every account.",
            introUr = "یونیورسٹی پورٹل، ای میل اور بینک میں ایک ہی کمزور پاس ورڈ استعمال کرنے سے ایک لیک تمام اکاؤنٹس کو خطرے میں ڈال دیتا ہے۔",
            explanation = "Attackers use automated credential stuffing tools to test leaked email-and-password pairs from one breached site against hundreds of others. Length and uniqueness matter far more than predictable symbol swaps.",
            explanationUr = "حملہ آور ایک ویب سائٹ سے لیک ہونے والے پاس ورڈز کو خودکار طریقے سے دیگر ویب سائٹس پر آزماتے ہیں۔ اس لیے ہر اکاؤنٹ کے لیے الگ اور طویل پاس ورڈ ضروری ہے۔",
            illustrativeExample = "Example: Using a unique 4-word passphrase per account (e.g., 'Cedar-Orbit-Lantern-49') stored in a reputable password manager.",
            illustrativeExampleUr = "مثال: ہر اکاؤنٹ کے لیے الگ اور طویل پاس فریز استعمال کرنا اور اسے قابلِ اعتماد پاس ورڈ مینیجر میں محفوظ رکھنا۔",
            warningSigns = listOf(
                "Using the same password for email, social media, and banking.",
                "Short passwords based on birthdates, phone numbers, or 'Password123'.",
                "Saving passwords in plain text notes or unencrypted chats."
            ),
            warningSignsUr = listOf(
                "ای میل، سوشل میڈیا اور بینکنگ کے لیے ایک ہی پاس ورڈ رکھنا۔",
                "تاریخِ پیدائش، فون نمبر یا عام الفاظ پر مشتمل چھوٹے پاس ورڈ۔",
                "سادہ نوٹس یا چیٹ میں پاس ورڈ محفوظ کرنا۔"
            ),
            protectionTips = listOf(
                "Use a unique password of at least 12–16 characters for every important account.",
                "Protect your primary email first — it is the reset key for everything else.",
                "Enable Multi-Factor Authentication (MFA) on email, banking, and social accounts."
            ),
            protectionTipsUr = listOf(
                "ہر اہم اکاؤنٹ کے لیے کم از کم 12 سے 16 حروف کا منفرد پاس ورڈ رکھیں۔",
                "سب سے پہلے اپنے بنیادی ای میل کو محفوظ بنائیں کیونکہ وہ باقی اکاؤنٹس کی کنجی ہے۔",
                "ای میل، بینکنگ اور سوشل اکاؤنٹس پر ملٹی فیکٹر آتھنٹیکیشن فعال کریں۔"
            ),
            knowledgeCheck = LessonQuestion(
                prompt = "Why is reusing the same strong password on every website still risky?",
                promptUr = "اگر ایک مضبوط پاس ورڈ ہر ویب سائٹ پر استعمال کیا جائے تو وہ پھر بھی خطرناک کیوں ہے؟",
                options = listOf(
                    "Browsers delete repeated passwords automatically",
                    "If one website suffers a data breach, attackers test that password everywhere else",
                    "Strong passwords expire faster when reused",
                    "It slows down Wi-Fi connections"
                ),
                optionsUr = listOf(
                    "براؤزر دہرائے گئے پاس ورڈ حذف کر دیتا ہے",
                    "اگر ایک سائٹ کا ڈیٹا لیک ہو جائے تو حملہ آور وہی پاس ورڈ دیگر اکاؤنٹس پر آزماتے ہیں",
                    "دہرانے سے مضبوط پاس ورڈ جلدی ختم ہو جاتے ہیں",
                    "اس سے وائی فائی کی رفتار کم ہو جاتی ہے"
                ),
                correctIndex = 1,
                explanation = "Credential stuffing attacks exploit password reuse after any single third-party breach.",
                explanationUr = "کسی ایک ویب سائٹ سے ڈیٹا لیک ہونے پر حملہ آور وہی پاس ورڈ دیگر اکاؤنٹس کھولنے کے لیے استعمال کرتے ہیں۔"
            )
        ),
        CyberLesson(
            id = "lesson_browsing",
            number = 8,
            title = "Safe Browsing & Digital Privacy",
            titleUr = "محفوظ براؤزنگ اور ڈیجیٹل پرائیویسی",
            intro = "Everyday habits—checking app permissions, avoiding pirated downloads, and locking down public Wi-Fi use—prevent most compromises.",
            introUr = "روزمرہ کی احتیاطی عادات—جیسے ایپ پرمیشنز چیک کرنا، مشکوک ڈاؤن لوڈز سے بچنا اور پبلک وائی فائی پر احتیاط—اکثر خطرات سے بچاتی ہیں۔",
            explanation = "Cracked software, fake browser update popups, and malicious APK downloads often bundle spyware that steals saved browser sessions and files. Keeping your OS updated and downloading apps only from official stores dramatically lowers risk.",
            explanationUr = "پائریٹڈ سافٹ ویئر، جعلی براؤزر اپ ڈیٹ پاپ اپس اور غیر مصدقہ ایپس میں اکثر اسپائی ویئر چھپا ہوتا ہے۔ ہمیشہ سرکاری اسٹور سے ایپس انسٹال کریں اور سسٹم اپ ڈیٹ رکھیں۔",
            illustrativeExample = "Example: A pop-up on a movie streaming page claiming 'Your browser is outdated — install Update.apk now.'",
            illustrativeExampleUr = "مثال: کسی غیر معروف ویب سائٹ پر پاپ اپ کہ 'آپ کا براؤزر پرانا ہے — ابھی Update.apk انسٹال کریں۔'",
            warningSigns = listOf(
                "Websites prompting you to download an .exe or .apk file to 'verify' or 'update'.",
                "Apps requesting SMS, Accessibility, or Contacts access without a clear reason.",
                "Entering banking logins on untrusted public Wi-Fi without HTTPS verification."
            ),
            warningSignsUr = listOf(
                "ویب سائٹس کا تصدیق یا اپ ڈیٹ کے نام پر .apk یا .exe فائل ڈاؤن لوڈ کروانا۔",
                "بغیر ضرورت ایس ایم ایس یا ایکسیسبیلٹی پرمیشن مانگنے والی ایپس۔",
                "غیر محفوظ پبلک وائی فائی پر بینکنگ لاگ ان استعمال کرنا۔"
            ),
            protectionTips = listOf(
                "Install apps and updates only from official app stores and device settings.",
                "Review app permissions periodically and revoke unnecessary access.",
                "Share minimal personal data on public social media profiles."
            ),
            protectionTipsUr = listOf(
                "ایپس اور اپ ڈیٹس صرف سرکاری ایپ اسٹور اور ڈیوائس سیٹنگز سے انسٹال کریں۔",
                "ایپ پرمیشنز کا باقاعدگی سے جائزہ لیں اور غیر ضروری رسائی ختم کریں۔",
                "عوامی سوشل میڈیا پروفائلز پر کم سے کم ذاتی معلومات شیئر کریں۔"
            ),
            knowledgeCheck = LessonQuestion(
                prompt = "A random webpage tells you to download a 'SecurityPlayer.apk' file to watch a lecture recording. What is the safest response?",
                promptUr = "اگر کوئی غیر معروف ویب سائٹ لیکچر دیکھنے کے لیے 'SecurityPlayer.apk' ڈاؤن لوڈ کرنے کا کہے تو محفوظ عمل کیا ہے؟",
                options = listOf(
                    "Install the APK and grant all requested permissions",
                    "Close the page immediately — do not install unverified APKs from pop-ups",
                    "Disable Play Protect first",
                    "Share the download with classmates"
                ),
                optionsUr = listOf(
                    "فائل انسٹال کر کے تمام اجازتیں دے دیں",
                    "فوراً صفحہ بند کریں — پاپ اپ سے غیر مصدقہ اے پی کے ہرگز انسٹال نہ کریں",
                    "پلے پروٹیکٹ بند کر دیں",
                    "ہم جماعتوں کو بھی لنک بھیجیں"
                ),
                correctIndex = 1,
                explanation = "Fake update or player APKs from web pop-ups are a leading way malware infects mobile devices.",
                explanationUr = "ویب پاپ اپس سے ملنے والی جعلی ایپس موبائل میں مالویئر پھیلانے کا بڑا ذریعہ ہیں۔"
            )
        )
    )

    val quizQuestions: List<QuizQuestion> = listOf(
        QuizQuestion(
            id = "q1",
            topic = "Phishing",
            topicUr = "فشنگ",
            question = "You receive an email from 'support@univ-portal-help.xyz' warning your student account will be deleted in 3 hours unless you verify your password. What is the best action?",
            questionUr = "آپ کو 'support@univ-portal-help.xyz' سے ای میل آتی ہے کہ 3 گھنٹوں میں پاس ورڈ تصدیق نہ کرنے پر اسٹوڈنٹ اکاؤنٹ حذف ہو جائے گا۔ بہترین اقدام کیا ہے؟",
            options = listOf(
                "Click the link and sign in quickly",
                "Ignore the link and check directly via your official university portal or IT desk",
                "Reply with your password so IT can verify it",
                "Forward the email to classmates"
            ),
            optionsUr = listOf(
                "لنک پر کلک کر کے فوراً لاگ ان کریں",
                "لنک کو نظرانداز کر کے براہِ راست یونیورسٹی کے سرکاری پورٹل یا آئی ٹی آفس سے تصدیق کریں",
                "جواب میں اپنا پاس ورڈ بھیج دیں",
                "ای میل دوستوں کو فارورڈ کریں"
            ),
            correctIndex = 1,
            explanation = "Mismatched domains (.xyz) combined with urgent account-deletion threats are classic phishing indicators.",
            explanationUr = "غیر متعلقہ ڈومین اور فوری اکاؤنٹ بند ہونے کی دھمکی فشنگ کی واضح علامات ہیں۔"
        ),
        QuizQuestion(
            id = "q2",
            topic = "OTP Protection",
            topicUr = "او ٹی پی تحفظ",
            question = "A caller claiming to be from your mobile wallet says they accidentally sent a 6-digit code to your phone and asks you to read it back. What should you do?",
            questionUr = "موبائل والٹ کا نمائندہ بن کر کال کرنے والا کہتا ہے کہ غلطی سے 6 ہندسوں کا کوڈ آپ کے فون پر آ گیا ہے، براہِ کرم بتا دیں۔ آپ کو کیا کرنا چاہیے؟",
            options = listOf(
                "Share the code if they know your name",
                "Refuse to share the code and hang up immediately",
                "Read only the first three digits",
                "Screenshot the SMS and send it on WhatsApp"
            ),
            optionsUr = listOf(
                "اگر انہیں آپ کا نام معلوم ہو تو کوڈ بتا دیں",
                "کوڈ بتانے سے صاف انکار کریں اور فوراً کال بند کر دیں",
                "صرف پہلے تین ہندسے بتائیں",
                "واٹس ایپ پر اسکرین شاٹ بھیج دیں"
            ),
            correctIndex = 1,
            explanation = "OTPs authorize logins or password resets on your account. Scammers often trigger an OTP and trick you into reading it.",
            explanationUr = "او ٹی پی آپ کے اکاؤنٹ تک رسائی یا پاس ورڈ ری سیٹ کی اجازت دیتا ہے؛ اسے کبھی کسی کو نہ بتائیں۔"
        ),
        QuizQuestion(
            id = "q3",
            topic = "Fake Scholarships",
            topicUr = "جعلی اسکالرشپ",
            question = "A WhatsApp forward says you won a government student laptop & cash grant, and asks for a PKR 1,800 processing fee via personal mobile wallet. How should you evaluate this?",
            questionUr = "واٹس ایپ پیغام میں سرکاری لیپ ٹاپ اور نقد گرانٹ جیتنے کا دعویٰ کیا جائے اور ذاتی موبائل والٹ میں 1,800 روپے فیس مانگی جائے تو یہ کیا ہے؟",
            options = listOf(
                "A normal bank fee for fast processing",
                "A high-risk advance-fee scam indicator",
                "Safe because the message used an official logo",
                "Safe if the sender uses an official-sounding name"
            ),
            optionsUr = listOf(
                "تیز کارروائی کے لیے معمول کی بینک فیس",
                "پیشگی فیس پر مبنی دھوکہ دہی کی شدید علامت",
                "محفوظ ہے کیونکہ اس میں سرکاری لوگو لگا ہے",
                "اگر بھیجنے والے کا نام سرکاری لگے تو محفوظ ہے"
            ),
            correctIndex = 1,
            explanation = "Genuine scholarships never ask recipients to transfer processing fees to personal wallets to claim an award.",
            explanationUr = "اصلی اسکالرشپس انعام جاری کرنے کے لیے ذاتی والٹ میں پروسیسنگ فیس نہیں مانگتیں۔"
        ),
        QuizQuestion(
            id = "q4",
            topic = "URL Safety",
            topicUr = "لنک کی حفاظت",
            question = "Which part of 'https://bank-support.verify-account.top/update' identifies the true controlling website?",
            questionUr = "'https://bank-support.verify-account.top/update' میں اصل کنٹرولنگ ویب سائٹ کون سا حصہ ہے؟",
            options = listOf(
                "bank-support",
                "verify-account.top",
                "update",
                "https"
            ),
            optionsUr = listOf(
                "bank-support",
                "verify-account.top",
                "update",
                "https"
            ),
            correctIndex = 1,
            explanation = "The root domain right before the first single '/' ('verify-account.top') is the real destination; 'bank-support' is only a subdomain.",
            explanationUr = "پہلے '/' سے پہلے والا اصل ڈومین ('verify-account.top') ہی حقیقی ویب سائٹ ہے؛ 'bank-support' صرف سب ڈومین ہے۔"
        ),
        QuizQuestion(
            id = "q5",
            topic = "URL Safety & HTTPS",
            topicUr = "HTTPS اور لنک کی حقیقت",
            question = "If a website URL starts with 'https://' and displays a padlock icon, what does that guarantee?",
            questionUr = "اگر کسی ویب سائٹ کے لنک میں 'https://' اور تالے کا نشان ہو تو اس سے کیا ثابت ہوتا ہے؟",
            options = listOf(
                "The organization is 100% honest and verified",
                "The connection is encrypted in transit, NOT that the site is trustworthy",
                "The website was scanned by your university",
                "It cannot steal passwords"
            ),
            optionsUr = listOf(
                "ویب سائٹ سو فیصد دیانتدار اور مستند ہے",
                "کنکشن انکرپٹڈ ہے، مگر یہ ویب سائٹ کے دیانتدار یا محفوظ ہونے کی ضمانت نہیں",
                "یونیورسٹی نے اس کی تصدیق کر دی ہے",
                "یہ پاس ورڈ نہیں چرا سکتی"
            ),
            correctIndex = 1,
            explanation = "Phishing sites commonly obtain free HTTPS certificates too. HTTPS protects data in transit, not against fraudulent intent.",
            explanationUr = "جعلی فشنگ ویب سائٹس بھی آسانی سے HTTPS سرٹیفکیٹ حاصل کر لیتی ہیں؛ اس لیے صرف تالے پر بھروسہ نہ کریں۔"
        ),
        QuizQuestion(
            id = "q6",
            topic = "Password Safety",
            topicUr = "پاس ورڈ کی حفاظت",
            question = "What is the strongest approach for managing passwords across email, university portals, and banking?",
            questionUr = "ای میل، یونیورسٹی پورٹل اور بینکنگ کے پاس ورڈز منظم کرنے کا سب سے محفوظ طریقہ کون سا ہے؟",
            options = listOf(
                "Use one memorable password everywhere so you never forget it",
                "Use a unique, long passphrase for every account and enable MFA",
                "Change only the last digit each year (e.g., 2025 -> 2026)",
                "Store all passwords in an unencrypted group chat"
            ),
            optionsUr = listOf(
                "ہر جگہ ایک ہی آسان پاس ورڈ استعمال کریں",
                "ہر اکاؤنٹ کے لیے الگ اور طویل پاس ورڈ رکھیں اور MFA فعال کریں",
                "ہر سال صرف آخری ہندسہ تبدیل کریں",
                "چیٹ میں تمام پاس ورڈ لکھ کر رکھیں"
            ),
            correctIndex = 1,
            explanation = "Unique passwords stop a breach on one service from compromising all your other accounts.",
            explanationUr = "ہر اکاؤنٹ کے لیے منفرد پاس ورڈ اور ملٹی فیکٹر آتھنٹیکیشن ایک لیک سے باقی اکاؤنٹس کو محفوظ رکھتا ہے۔"
        ),
        QuizQuestion(
            id = "q7",
            topic = "Fake Job Offers",
            topicUr = "جعلی ملازمت",
            question = "An unknown recruiter messages on WhatsApp offering a remote internship with zero interview if you pay PKR 2,500 for onboarding equipment. What is this pattern?",
            questionUr = "کوئی نامعلوم شخص واٹس ایپ پر بغیر انٹرویو ریموٹ انٹرن شپ دے کر 2,500 روپے سامان کی فیس مانگے تو یہ کس قسم کا خطرہ ہے؟",
            options = listOf(
                "Standard corporate HR onboarding",
                "Employment advance-fee scam",
                "Verified campus placement",
                "Safe freelance contract"
            ),
            optionsUr = listOf(
                "معیاری کارپوریٹ ایچ آر طریقہ کار",
                "ملازمت کے نام پر پیشگی فیس کا دھوکہ",
                "مصدقہ کیمپس پلیسمنٹ",
                "محفوظ فری لانس معاہدہ"
            ),
            correctIndex = 1,
            explanation = "Paying upfront fees to start an unsolicited job or internship is a hallmark of employment fraud.",
            explanationUr = "انٹرن شپ یا نوکری شروع کرنے کے لیے پیشگی فیس مانگنا جعلی بھرتی کی بڑی نشانی ہے۔"
        ),
        QuizQuestion(
            id = "q8",
            topic = "Online Payments",
            topicUr = "آن لائن ادائیگیاں",
            question = "A 'courier' SMS says your parcel needs PKR 450 customs clearance and links to http://198.51.100.12/pay. What warning signs are present?",
            questionUr = "کوریئر ایس ایم ایس میں 450 روپے کسٹم فیس کے لیے http://198.51.100.12/pay لنک دیا گیا ہے۔ اس میں کون سی خطرناک علامات ہیں؟",
            options = listOf(
                "Only the fee amount is unusual",
                "Unencrypted HTTP, raw IP address host, and unsolicited payment prompt",
                "Nothing — IP addresses are faster than domains",
                "It is safe because PKR 450 is a small amount"
            ),
            optionsUr = listOf(
                "صرف رقم غیر معمولی ہے",
                "غیر محفوظ HTTP، ڈومین کے بجائے IP ایڈریس اور غیر متوقع ادائیگی کا لنک",
                "کوئی خطرہ نہیں — IP ایڈریس زیادہ تیز ہوتا ہے",
                "450 روپے چھوٹی رقم ہے اس لیے محفوظ ہے"
            ),
            correctIndex = 1,
            explanation = "Scammers use small fees to steal card credentials through raw IP or lookalike payment pages.",
            explanationUr = "دھوکہ باز چھوٹی فیس کے بہانے کارڈ یا بینک تفصیلات چرانے کے لیے IP لنکس استعمال کرتے ہیں۔"
        ),
        QuizQuestion(
            id = "q9",
            topic = "Social Engineering",
            topicUr = "سوشل انجینئرنگ",
            question = "Someone pretending to be a friend messages from a new number asking for urgent mobile wallet money because they 'lost their phone and cannot call.' What should you do first?",
            questionUr = "کوئی شخص نئے نمبر سے آپ کا دوست بن کر فوری رقم مانگے اور کہے کہ فون گم ہو گیا ہے کال نہیں کر سکتا، تو سب سے پہلے کیا کریں؟",
            options = listOf(
                "Transfer immediately to help them",
                "Verify by calling their old number or asking another trusted mutual contact",
                "Share your bank card photo",
                "Ask for their OTP"
            ),
            optionsUr = listOf(
                "مدد کے لیے فوراً رقم بھیج دیں",
                "ان کے پرانے نمبر پر کال کر کے یا کسی مشترکہ دوست کے ذریعے تصدیق کریں",
                "اپنے بینک کارڈ کی تصویر بھیجیں",
                "ان کا او ٹی پی مانگیں"
            ),
            correctIndex = 1,
            explanation = "Out-of-band verification defeats impersonation scams where attackers pretend to be friends or relatives in distress.",
            explanationUr = "کسی اور ذریعے سے تصدیق کرنے سے شناخت کی نقل کرنے والے دھوکہ باز فوراً پکڑے جاتے ہیں۔"
        ),
        QuizQuestion(
            id = "q10",
            topic = "Digital Privacy",
            topicUr = "ڈیجیٹل پرائیویسی",
            question = "Which personal detail should you avoid posting publicly on social media because attackers use it to impersonate you or pass verification checks?",
            questionUr = "سوشل میڈیا پر کون سی ذاتی معلومات عوامی طور پر شیئر کرنے سے گریز کرنا چاہیے کیونکہ حملہ آور ان سے آپ کی شناخت کا غلط فائدہ اٹھا سکتے ہیں؟",
            options = listOf(
                "Your favorite book title",
                "Photos of your CNIC, student ID barcode, or boarding pass",
                "Public university event flyers",
                "General study tips"
            ),
            optionsUr = listOf(
                "پسندیدہ کتاب کا نام",
                "شناختی کارڈ، اسٹوڈنٹ آئی ڈی بارکوڈ یا بورڈنگ پاس کی تصاویر",
                "یونیورسٹی کے عوامی ایونٹ پوسٹرز",
                "مطالعے کے عمومی مشورے"
            ),
            correctIndex = 1,
            explanation = "Identity documents, barcodes, and full birthdates help scammers open fraudulent accounts or craft targeted phishing.",
            explanationUr = "شناختی دستاویزات اور بارکوڈز دھوکہ بازوں کو جعلی اکاؤنٹس بنانے اور نشانہ بنانے میں مدد دیتے ہیں۔"
        ),
        QuizQuestion(
            id = "q11",
            topic = "Malware Awareness",
            topicUr = "مالویئر سے آگاہی",
            question = "A pop-up ad claims your Android phone has 9 viruses and tells you to install 'CleanFix.apk' immediately. What is actually happening?",
            questionUr = "ایک پاپ اپ اشتہار دعویٰ کرے کہ آپ کے فون میں 9 وائرس ہیں اور فوراً 'CleanFix.apk' انسٹال کرنے کو کہے، تو حقیقت کیا ہے؟",
            options = listOf(
                "A legitimate Android OS hardware scan",
                "Scareware attempting to trick you into installing malicious software",
                "Google Play automatic update",
                "A battery calibration alert"
            ),
            optionsUr = listOf(
                "اینڈرائیڈ سسٹم کا اصل اسکین",
                "خوف پھیلا کر آپ سے خطرناک سافٹ ویئر انسٹال کروانے کا جال (Scareware)",
                "گوگل پلے کی خودکار اپ ڈیٹ",
                "بیٹری کیلیبریشن الرٹ"
            ),
            correctIndex = 1,
            explanation = "Websites cannot scan your Android system for viruses; fake virus alerts are scareware lures.",
            explanationUr = "ویب سائٹس آپ کے موبائل کو وائرس کے لیے اسکین نہیں کر سکتیں؛ ایسے پاپ اپس خوف پھیلا کر مالویئر انسٹال کرواتے ہیں۔"
        ),
        QuizQuestion(
            id = "q12",
            topic = "URL Shorteners & Authority",
            topicUr = "مختصر لنکس اور تصدیق",
            question = "Why should you be cautious with URL shorteners (like bit.ly) or links containing '@' symbols in unexpected messages?",
            questionUr = "غیر متوقع پیغامات میں مختصر لنکس (جیسے bit.ly) یا '@' علامت والے لنکس سے احتیاط کیوں ضروری ہے؟",
            options = listOf(
                "They always delete browser bookmarks",
                "They conceal the true destination host before you click",
                "They disable mobile data",
                "They only work on desktop computers"
            ),
            optionsUr = listOf(
                "یہ براؤزر بک مارکس حذف کر دیتے ہیں",
                "یہ کلک کرنے سے پہلے اصل ویب سائٹ کا پتہ چھپا لیتے ہیں",
                "یہ موبائل ڈیٹا بند کر دیتے ہیں",
                "یہ صرف کمپیوٹر پر چلتے ہیں"
            ),
            correctIndex = 1,
            explanation = "Shorteners hide the real domain, while '@' in URL authority syntax can trick users into misreading the target host.",
            explanationUr = "مختصر لنکس اور '@' علامت اصل ڈومین کو چھپا کر صارف کو دھوکا دینے کے لیے استعمال ہو سکتے ہیں۔"
        )
    )
}
