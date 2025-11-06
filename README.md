# E-Commerce Fulfillment Service

## 🎯 Overview

Fulfillment service for e-commerce platform with **unique dual-confirmation delivery system** and **alternate recipient support**.

## ✨ Key Features

### 1. Dual-Confirmation Delivery System
- Delivery can only be marked as "DELIVERED" when both customer AND agent confirm
- Proximity verification required (parties must be within 50m of each other)
- Flexible location (parties can meet anywhere, not just at delivery address)
- Prevents fake deliveries, wrongful refunds, and false "not available" claims

### 2. Alternate Recipient Support
- Customer can share delivery link with unlimited alternate phone numbers/users
- Any alternate user can receive and confirm delivery
- Same dual confirmation criteria applies
- Agent proximity must match with any of the alternate users' proximity

### 3. Age Verification
- Support for restricted items (alcohol, etc.)
- Multiple verification methods:
  - Aadhaar Face RD (UIDAI official service)
  - Photo Verification (ID + Selfie)
  - ID Verification (OCR)
  - Video KYC

### 4. Public Tracking
- Track delivery using just tracking number (no authentication required)

## 🚀 Quick Start

### Prerequisites
- Java 25+
- Maven 3.9+
- PostgreSQL
- Kafka (optional, for events)

### Build
```bash
mvn clean package
```

### Run
```bash
mvn spring-boot:run
```

Application starts on port **8091**

## 📚 Documentation

- [Dual-Confirmation Design](./DUAL_CONFIRMATION_UPDATED_DESIGN.md)
- [Alternate Recipient Feature](./ALTERNATE_RECIPIENT_FEATURE.md)
- [Aadhaar Face RD Integration](./AADHAAR_FACE_RD_INTEGRATION_GUIDE.md)
- [Implementation Status](./DUAL_CONFIRMATION_IMPLEMENTATION_STATUS.md)
- [Complete Implementation Status](./COMPLETE_IMPLEMENTATION_STATUS.md)

## 📡 API Endpoints

### Dual Confirmation
- `POST /api/v1/delivery/{deliveryId}/confirm` - Agent confirm
- `POST /api/v1/delivery/{deliveryId}/customer-confirm` - Customer confirm
- `GET /api/v1/delivery/{deliveryId}/confirmation-status` - Get status

### Alternate Recipients
- `POST /api/v1/delivery/{deliveryId}/share-link` - Share link
- `GET /api/v1/public/delivery/share/{shareToken}` - Get share details (public)
- `POST /api/v1/public/delivery/share/{shareToken}/confirm` - Alternate confirm (public)

### Public
- `GET /api/v1/delivery/public/tracking/{trackingNumber}` - Public tracking

See [Insomnia Collection](../insomnia-collections/Fulfillment.json) for complete API documentation.

## 🗄️ Database

Migrations are managed by Flyway:
- V1-V7: Core tables
- V8: Delivery confirmations
- V9: Age verifications
- V10: Alternate recipients

## ✅ Status

**Build**: ✅ SUCCESS  
**Runtime**: ✅ RUNNING  
**Core Features**: ✅ COMPLETE

---

*Last Updated: 2025-11-06*
