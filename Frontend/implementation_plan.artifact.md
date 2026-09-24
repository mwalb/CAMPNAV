# Implementation Plan - Animated Landing Page and Reporting Screen

This plan details the addition of a new, fully animated Landing Page as the default entry point for the CAMPNAV app, along with a Reporting Screen for sharing location-based issues.

## Proposed Changes

### Core Navigation
#### [MODIFY] [App.kt](file:///home/kali/CAMPNAV/Frontend/shared/src/commonMain/kotlin/org/com/App.kt)
- Add `Landing` and `Report` to `AppMode` enum.
- Update `MainApp` to handle the new screens.
- Set `AppMode.Landing` as the default starting mode.

### New Screens
#### [NEW] [LandingScreen.kt](file:///home/kali/CAMPNAV/Frontend/shared/src/commonMain/kotlin/org/com/campus/presentation/LandingScreen.kt)
- Implement a visually rich, animated landing page.
- Features:
    - Animated "CAMPNAV" title and logo.
    - Two primary action cards: "Explore Campuses" and "Report an Issue".
    - Staggered entrance animations for all UI elements.
    - Hover/Press effects for interaction.

#### [NEW] [ReportScreen.kt](file:///home/kali/CAMPNAV/Frontend/shared/src/commonMain/kotlin/org/com/campus/presentation/ReportScreen.kt)
- Implement a screen for reporting campus issues via location sharing.
- Features:
    - Display current Latitude and Longitude (fetching logic integrated).
    - Form fields for "Issue Category" and "Description".
    - "Submit Report" button with success animation/feedback.
    - Back button to return to the Landing page.

### UI Enhancements
#### [MODIFY] [UniversitySelectionScreen.kt](file:///home/kali/CAMPNAV/Frontend/shared/src/commonMain/kotlin/org/com/campus/presentation/UniversitySelectionScreen.kt)
- Add entrance animations to match the "fully animated" requirement.
- Ensure styling matches the provided design image exactly (Deep Navy background, specific card layout).

## Verification Plan

### Automated Tests
- N/A (UI-focused changes).

### Manual Verification
- Deploy the app to an Android device/emulator.
- Verify the Landing Page appears first with animations.
- Navigate to "Explore Campuses" and verify the University Selection screen animations.
- Navigate to "Report an Issue", verify location fetching (on Android), and test the submission flow.
