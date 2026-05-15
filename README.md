# Kutira-Kone
### Zero-Waste Fabric Exchange | Hyper-Local Marketplace Powered by GenAI

Kutira-Kone is a GenAI-powered Android application designed to reduce textile waste by connecting tailors, boutique owners, artisans, and crafters through a hyper-local fabric scrap exchange marketplace. The platform enables users to upload leftover fabric scraps, discover nearby reusable materials, exchange or sell scraps, and explore AI-generated upcycling ideas.

---

## 📱 Features

### 🔐 Authentication
- Firebase Phone OTP Login
- Guest Browsing Support
- Secure User Authentication

### 🧵 Fabric Scrap Marketplace
- Upload fabric scraps with images
- Add size, color, material, and condition details
- Swap, Free, or Sell listing modes
- Real-time listing updates

### 🤖 GenAI Features
- AI-powered fabric classification using Gemini AI
- Auto-detect material type:
  - Silk
  - Cotton
  - Wool
  - Synthetic
- AI-generated upcycling project ideas

### 🗺️ Hyper-Local Discovery
- Google Maps integration
- Radius-based search (1–25 km)
- Nearby fabric scrap discovery
- Privacy-protected neighborhood sharing

### 🔍 Search & Filters
- Material type filtering
- Color and condition filtering
- Radius search
- Smart catalog browsing

### 💬 Communication
- In-app buyer-seller chat
- Trade request notifications
- Real-time updates using Firebase

### 🌱 Sustainability Features
- Sustainability score tracking
- Textile waste reduction metrics
- Community-driven circular economy support

---

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Kotlin | Android App Development |
| Firebase Auth | OTP Authentication |
| Firebase Firestore | Real-time Database |
| Firebase Storage | Image Storage |
| Firebase Cloud Messaging | Push Notifications |
| Gemini AI | Fabric Classification & AI Ideas |
| Google Maps SDK | Map Integration |
| MVVM Architecture | Clean App Structure |
| Jetpack Compose | Modern Android UI |

---

## 📂 Project Structure

```bash
Kutira-Kone/
│
├── app/
├── ui/
├── viewmodel/
├── repository/
├── firebase/
├── models/
├── screens/
├── adapters/
├── utils/
└── assets/
