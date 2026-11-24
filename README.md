# My Downline Manager – Advanced Starter (v2)

Team Banjarahills Android Kotlin project.

## Features included
- Import selected phone contacts after permission is granted.
- Duplicate phone number check (last 10 digits).
- Offline local member storage.
- Search by member name or phone number.
- Dashboard totals and current-month CNC Done/Pending counts.
- Mark CNC Done / Undo, Call via Android dialer, Share reminder, Delete confirmation.
- Telugu Text-to-Speech reminder preview (voice depends on the phone's installed TTS engine).
- Monthly reminder notification scheduled for 9:00 AM on dates 2–14, with next reminder rescheduled after each alert.
- Reschedule reminder after device reboot.
- Share/export member list as JSON text backup.

## Build APK using GitHub Actions
1. Upload the contents of this project folder into the root of your GitHub repository (not the ZIP file).
2. Commit the uploaded files to branch `main`.
3. Open the repository's **Actions** tab.
4. Select **Build Android APK** and run it, or wait for a push build.
5. When the run completes successfully, open it and download the artifact named `My-Downline-Manager-debug-apk`.
6. Extract the artifact ZIP and install `app-debug.apk` on your Android phone.

## Important
This is a source project, not an APK. It has not been compiled or tested on a physical device in this environment. Android battery restrictions and notification permissions may affect reminder delivery. TTS Telugu voice quality/gender depends on the installed engine. Call opens the dialer; it does not automatically call or play voice to the other person. Share opens the Android share sheet; it does not send messages automatically. Cloud sync, Firebase, and remote auto-calling are not included.
