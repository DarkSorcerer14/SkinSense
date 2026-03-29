# Skin Sense
**By Code Bros (Ayush Aryan & Vivek Kumar Prusty)**

**Skin Sense** is a privacy-first, on-device AI mobile application designed to detect common skin conditions. It is specifically optimized for diverse Indian skin tones and engineered to run offline on low-end Android devices, bridging the healthcare gap in rural and low-resource areas.

---

## The Problem
* **Limited Access:** 1.2 billion people share only 7,000 dermatologists in India.
* **Algorithmic Bias:** Western AI models often fail on Indian skin tones, leading to frequent misdiagnoses.
* **Economic Impact:** Over 400M+ cases go undiagnosed annually, leading to complications and a massive ₹5,000 crore yearly economic loss.

## Our Solution
Skin Sense uses lightweight, on-device AI to analyze skin images instantly:
- **100% Offline & Private:** No cloud uploads. Inference happens natively on the phone.
- **Optimized for India:** Trained on datasets featuring Fitzpatrick Types III-VI.
- **Actionable Guidance:** Provides probable conditions, severity estimation, and affordable first-line care options.

---

## Target Audience
* **Primary:** Rural/semi-urban residents, students, and outdoor workers (farmers, delivery staff).
* **Secondary:** Community health workers (ASHAs), telemedicine providers, and small clinics.

## Tech Stack

### AI & Machine Learning
* **Frameworks:** TensorFlow, Keras
* **Model Arch:** MobileNetV3 (quantized via TensorFlow Lite)
* **Environment:** Python / Google Colab

### Mobile Application
* **Platform:** Android Operating System
* **Languages:** Kotlin & Java
* **Key Integrations:** CameraX API for seamless real-time edge inference

### Hardware Profile
* Engineered to perform at **30-60 FPS** entirely offline on highly-affordable 4GB+ RAM smartphones.

---
*Developed for the goal of bringing us One Step Closer To Better Skin Health.*