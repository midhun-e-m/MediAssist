🏥 MediAssist – AI Powered Medical Assistant

An AI-powered Android application that provides basic first-aid guidance, emergency assistance features, and real-time medical support tools.it can sent emergency request to added contacts via messages 

📱 Overview

MediAssist is an Android application built using Kotlin that integrates:

🤖 AI-powered first aid assistant (Groq LLM)

🔐 Firebase Authentication

🗺 Nearby hospitals with OpenStreetMap

🚑 Emergency contacts & quick actions

📦 Real-time features & tracking modules

The goal of this project is to combine AI + mobile development + real-world emergency utility into a single intelligent healthcare assistant.

🚀 Features
🤖 AI Medical Assistant

Integrated Groq LLM (Llama 3.3 70B)

Provides basic first aid guidance

Avoids medical diagnosis

Handles emergency escalation prompts

🔐 Authentication

Firebase Email/Password login

Secure session handling

🏥 Nearby Hospitals

OpenStreetMap integration (OSMDroid)

Road-based distance calculation

Location-based hospital listing

📞 Emergency Utilities

Quick access to emergency contacts

Emergency history tracking

📍 Location & Tracking

Real-time location services

Driver / patient modules (if enabled)

🛠 Tech Stack
Layer	Technology
Language	Kotlin
Architecture	MVVM (modular structure)
Networking	Retrofit + OkHttp
AI Integration	Groq API (Llama 3.3 70B)
Maps	OSMDroid (OpenStreetMap)
Authentication	Firebase Auth
Database	Firebase Firestore
Async	Kotlin Coroutines
🔐 Security Implementation

API keys stored securely in local.properties

Accessed via BuildConfig

Sensitive keys excluded using .gitignore

No hardcoded secrets inside repository

🏗 Architecture Overview

Android App
→ Retrofit Network Layer
→ Groq API (LLM)

Firebase services handle:

Authentication

Firestore data storage

Modular package structure:

ui/
auth/
driver/
user/
data/
remote/
model/
📸 Screenshots




⚙️ Setup Instructions

Clone the repository

Open in Android Studio

Add your Groq API key inside:

local.properties
GROQ_API_KEY=your_api_key_here

Sync Gradle

Run on device/emulator

📌 Future Improvements

Backend proxy server for API key isolation

Offline first-aid knowledge caching

Push notifications for emergency alerts

Improved UI/UX refinement

Role-based dashboards

🎯 Learning Outcomes

Secure API key handling in Android

AI model integration using REST APIs

Firebase authentication & Firestore usage

OpenStreetMap integration without Google Maps

Handling real-world app architecture

👨‍💻 Developer

Midhun
MCA Student | Android Developer | Backend Enthusiast

⭐ If you found this project interesting, consider giving it a star!
