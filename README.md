# Heal the World Offline Assistant (draft v0.1)

A non-profit, fully offline Android app that helps a frightened person find the next step in an emergency.

**Status: early prototype. The cards in `app/src/main/assets/cards.json` are DRAFTS and have NOT been reviewed by qualified experts. Do not distribute as an emergency tool until they are.**

## How it works
- The person types what is happening (English or Dutch for now).
- A fixed rule layer (`Cards.kt`) picks the most urgent matching card. No AI yet.
- The card shows one step at a time, with optional read-aloud.
- The app has **no internet permission** and stores only language and emergency number on the phone.

## Build an APK without installing anything
1. Upload this folder to a GitHub repository.
2. Open the **Actions** tab, run **Build APK**, and download the APK from the finished run.

## Adding or fixing cards
Edit `cards.json`. Set `"reviewed": true` only after a qualified reviewer has signed off.

## Not yet included
Speech input, more languages, pictures, "show to a helper" screen, on-device AI matcher.
