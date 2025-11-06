# Alternate Recipient Feature - Implementation

## ✅ Feature Summary

**Requirement**: If customer is not available at delivery location, they can share a link with alternate phone numbers/users (no limit on number of alternates). Any alternate user can receive the order and mark as delivered, provided same dual confirmation criteria is met - delivery agent proximity must match with any of the alternate users' proximity.

## 🎯 Key Features

1. **Unlimited Alternate Recipients**: Customer can share with any number of alternate phone numbers/users
2. **Shareable Links**: Generate unique shareable links for each alternate recipient
3. **Dual Confirmation**: Same dual confirmation applies - agent and alternate recipient must both confirm
4. **Proximity Check**: Agent proximity must match with ANY of the alternate users' proximity
5. **No Account Required**: Alternate recipients can confirm using just phone number (no account needed)
6. **Link Expiry**: Links expire after configurable time (default 24 hours)
7. **Revocable**: Customer/admin can revoke shared links

## 📊 Database Schema

### Table: `alternate_recipients`
- Stores alternate recipient details
- Share tokens and links
- Confirmation status and location
- Expiry and revocation tracking

### Updated: `delivery_confirmations`
- Added `alternate_recipient_id` field
- Added `confirmed_by_alternate` flag
- Added alternate recipient name and phone

## 🔄 Flow

### 1. Customer Shares Link
```
Customer → Share delivery link → Enter alternate phone numbers → 
System generates unique tokens → Send links via SMS/Email/WhatsApp
```

### 2. Alternate Recipient Receives Link
```
Alternate recipient opens link → Enters phone number (if not logged in) → 
Views delivery details → Waits for agent
```

### 3. Agent Arrives
```
Agent arrives at location → Marks "Arrived" with GPS → 
System checks proximity with ALL active alternate recipients → 
If any match → Notify matched alternate recipient
```

### 4. Dual Confirmation
```
Agent confirms delivery (with GPS) → 
Alternate recipient confirms delivery (with GPS) → 
System verifies proximity between agent and alternate → 
If both confirmed AND in proximity → DELIVERED ✅
```

## 📱 API Endpoints

### Share Delivery Link
```
POST /api/v1/delivery/{deliveryId}/share-link
Body: {
    "alternateRecipients": [
        {
            "name": "John Doe",
            "phoneNumber": "+919876543210",
            "email": "john@example.com",
            "userId": "uuid" // Optional
        }
    ],
    "shareMethod": "SMS", // SMS, EMAIL, WHATSAPP, LINK
    "expiryHours": 24
}
```

### Get Share Link Details (Public)
```
GET /api/v1/public/delivery/share/{shareToken}
Response: {
    "deliveryId": "...",
    "recipientName": "...",
    "status": "ACTIVE",
    "expiresAt": "..."
}
```

### Alternate Recipient Confirm Delivery
```
POST /api/v1/public/delivery/share/{shareToken}/confirm
Body: {
    "latitude": 28.6139,
    "longitude": 77.2090,
    "locationAccuracy": 5.2,
    "phoneNumber": "+919876543210" // For verification
}
```

### Revoke Share Link
```
DELETE /api/v1/delivery/{deliveryId}/share-link/{recipientId}
```

## 🔒 Security

1. **Token Security**: Unique, unguessable tokens
2. **Phone Verification**: Verify phone number when confirming
3. **Link Expiry**: Links expire after set time
4. **Proximity Verification**: Same strict proximity check as regular delivery
5. **Revocation**: Customer can revoke links anytime

## ✅ Implementation Status

- ✅ Database schema (V10 migration)
- ✅ AlternateRecipient entity
- ✅ AlternateRecipientRepository
- ✅ AlternateRecipientService interface
- ✅ ProximityService updated for alternate recipients
- ✅ Request/Response DTOs
- ⏳ Service implementation (in progress)
- ⏳ Controller endpoints (pending)
- ⏳ Integration with DeliveryConfirmationService (pending)

---

*Last Updated: 2025-11-06*

