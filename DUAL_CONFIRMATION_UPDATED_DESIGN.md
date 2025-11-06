# Dual-Confirmation Delivery System - Updated Design

## 🎯 Key Updates

### 1. Flexible Delivery Location ✅
**Change**: Proximity check is now between **agent and customer** (not just delivery address)

**Scenario**: 
- Customer is not at home
- Customer and agent coordinate to meet at different location
- Both are in proximity to each other → Can mark as delivered

**Logic**:
- Check if agent and customer are within 50m of each other (anywhere)
- Still record actual delivery location (where they met)
- Delivery address remains for records, but actual delivery can happen anywhere

### 2. Age Verification for Restricted Items ✅
**Requirement**: Items like alcohol require age verification (18+ or 21+)

**Methods**:
1. **Photo Verification** (Customer photo or ID photo)
2. **Aadhaar Biometric Check** (Face ID match with Aadhaar)
3. **Government ID Verification** (Aadhaar, PAN, Driving License)
4. **Real-time Face Verification** (Face match against ID photo)

---

## 📐 Updated System Design

### Proximity Verification (Updated)

**Old Logic** ❌:
```
Both must be within 50m of delivery address
```

**New Logic** ✅:
```
Agent and customer must be within 50m of EACH OTHER (anywhere)
+ Record actual delivery location (where they met)
```

### Age Verification Flow

```
1. Check if order contains restricted items (alcohol, etc.)
   ↓
2. If yes → Age verification required
   ↓
3. Customer must verify age before delivery can be confirmed
   ↓
4. Verification methods:
   a. Take customer photo + ID photo
   b. Aadhaar biometric check (face ID)
   c. Government ID verification
   ↓
5. System verifies age
   ↓
6. If verified → Allow delivery confirmation
   If not verified → Block delivery, return to warehouse
```

---

## 🗄️ Updated Database Schema

### Update `delivery_confirmations` Table

```sql
-- Add actual delivery location (where they actually met)
ALTER TABLE delivery_confirmations
ADD COLUMN actual_delivery_latitude DECIMAL(10, 8),
ADD COLUMN actual_delivery_longitude DECIMAL(11, 8),
ADD COLUMN actual_delivery_address TEXT,
ADD COLUMN delivery_location_type VARCHAR(50) DEFAULT 'SCHEDULED_ADDRESS';
-- SCHEDULED_ADDRESS, ALTERNATE_LOCATION, CUSTOMER_LOCATION

-- Add age verification fields
ADD COLUMN requires_age_verification BOOLEAN DEFAULT FALSE,
ADD COLUMN minimum_age_required INTEGER, -- 18 or 21
ADD COLUMN age_verified BOOLEAN DEFAULT FALSE,
ADD COLUMN age_verification_method VARCHAR(50),
-- PHOTO_VERIFICATION, AADHAAR_BIOMETRIC, ID_VERIFICATION, VIDEO_KYC
ADD COLUMN age_verification_status VARCHAR(50),
-- PENDING, VERIFIED, FAILED, REJECTED
ADD COLUMN age_verification_at TIMESTAMP,
ADD COLUMN customer_photo_url VARCHAR(500),
ADD COLUMN id_photo_url VARCHAR(500),
ADD COLUMN aadhaar_reference_id VARCHAR(100),
ADD COLUMN age_verification_notes TEXT;
```

### New Table: `age_verifications`

```sql
CREATE TABLE age_verifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_confirmation_id UUID NOT NULL REFERENCES delivery_confirmations(id),
    delivery_id UUID NOT NULL REFERENCES deliveries(id),
    tenant_id UUID NOT NULL,
    customer_user_id UUID NOT NULL,
    
    -- Age requirement
    minimum_age_required INTEGER NOT NULL, -- 18 or 21
    customer_date_of_birth DATE, -- Extracted from verification
    
    -- Verification method
    verification_method VARCHAR(50) NOT NULL,
    -- PHOTO_VERIFICATION, AADHAAR_BIOMETRIC, ID_VERIFICATION, VIDEO_KYC
    
    -- Photo verification
    customer_photo_url VARCHAR(500),
    id_photo_url VARCHAR(500),
    id_type VARCHAR(50), -- AADHAAR, PAN, DRIVING_LICENSE, PASSPORT
    id_number VARCHAR(100),
    
    -- Aadhaar biometric
    aadhaar_number VARCHAR(12),
    aadhaar_reference_id VARCHAR(100), -- UIDAI reference ID
    biometric_match_score DECIMAL(5, 2), -- Face match confidence (0-100)
    aadhaar_verified BOOLEAN DEFAULT FALSE,
    
    -- Verification result
    verification_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    -- PENDING, VERIFIED, FAILED, REJECTED, MANUAL_REVIEW
    age_verified BOOLEAN DEFAULT FALSE,
    verified_age INTEGER, -- Calculated age
    verification_confidence DECIMAL(5, 2), -- Overall confidence score
    
    -- Metadata
    verified_by_system BOOLEAN DEFAULT TRUE,
    verified_by_admin UUID, -- Admin user ID if manual verification
    verification_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP,
    
    CONSTRAINT fk_age_verification_confirmation FOREIGN KEY (delivery_confirmation_id) 
        REFERENCES delivery_confirmations(id) ON DELETE CASCADE
);

CREATE INDEX idx_age_verification_delivery_id ON age_verifications(delivery_id);
CREATE INDEX idx_age_verification_status ON age_verifications(verification_status);
CREATE INDEX idx_age_verification_customer ON age_verifications(customer_user_id);
```

---

## 🔄 Updated Proximity Logic

### ProximityService Updates

```java
/**
 * Check if agent and customer are in proximity to each other
 * (Not limited to delivery address - can be anywhere)
 */
public boolean verifyPartiesProximity(
    double agentLat, double agentLon,
    double customerLat, double customerLon,
    int proximityRadius
) {
    double distance = calculateDistance(agentLat, agentLon, customerLat, customerLon);
    return distance <= proximityRadius;
}

/**
 * Verify proximity and record actual delivery location
 */
public ProximityResult verifyAndRecordLocation(
    double agentLat, double agentLon,
    double customerLat, double customerLon,
    double scheduledDeliveryLat, double scheduledDeliveryLon,
    int proximityRadius
) {
    // Check if parties are close to each other
    boolean partiesInProximity = verifyPartiesProximity(
        agentLat, agentLon, customerLat, customerLon, proximityRadius
    );
    
    // Calculate distance between parties
    double distanceBetweenParties = calculateDistance(
        agentLat, agentLon, customerLat, customerLon
    );
    
    // Calculate distance from scheduled address
    double distanceFromScheduled = calculateDistance(
        scheduledDeliveryLat, scheduledDeliveryLon,
        (agentLat + customerLat) / 2, // Midpoint
        (agentLon + customerLon) / 2
    );
    
    // Determine location type
    String locationType = distanceFromScheduled > 100 ? 
        "ALTERNATE_LOCATION" : "SCHEDULED_ADDRESS";
    
    return new ProximityResult(
        partiesInProximity,
        distanceBetweenParties,
        distanceFromScheduled,
        locationType,
        (agentLat + customerLat) / 2, // Actual delivery lat
        (agentLon + customerLon) / 2  // Actual delivery lon
    );
}
```

---

## 🔐 Age Verification Methods

### Method 1: Photo Verification (Simple)

**Flow**:
1. Customer takes selfie
2. Customer takes photo of ID (Aadhaar/PAN/DL)
3. System extracts DOB from ID using OCR
4. System verifies face match between selfie and ID photo
5. Calculate age from DOB
6. If age >= minimum → Verified ✅

**Pros**: Simple, fast, no external API
**Cons**: Less secure, can be manipulated

### Method 2: Aadhaar Face RD (Most Secure & Recommended) ⭐⭐⭐

**Aadhaar Face RD (Registered Device)** is UIDAI's official app for face authentication. It's now available for private entities to integrate into their mobile applications.

**Key Features**:
- ✅ **Live Face Capture**: Captures real-time face image
- ✅ **Aadhaar Matching**: Matches against Aadhaar biometric database
- ✅ **Privacy-First**: User controls data sharing, requires consent
- ✅ **Government-Verified**: Official UIDAI service
- ✅ **No Physical Documents**: Eliminates need for ID cards
- ✅ **Instant Verification**: Fast and seamless

**Prerequisites**:
1. Register as **AUA (Authentication User Agency)** or **KUA (KYC User Agency)** via Aadhaar Good Governance portal
2. Obtain UIDAI approval and credentials
3. Integrate Aadhaar Face RD SDK/App into delivery app

**Flow**:
1. Customer opens delivery app
2. System detects age-restricted item → Triggers age verification
3. Customer selects "Aadhaar Face RD" verification method
4. System invokes Aadhaar Face RD app/service
5. Customer provides:
   - Aadhaar number (or scans QR code)
   - Consent for face authentication
6. Aadhaar Face RD app:
   - Captures live face image
   - Sends to UIDAI for verification
   - Matches against Aadhaar biometric database
7. UIDAI returns:
   - Authentication result (success/failure)
   - Match confidence score
   - DOB (if authorized)
   - Name (if authorized)
8. System calculates age from DOB
9. If age >= minimum AND face match success → Verified ✅

**Pros**: 
- Most secure (government-verified)
- Tamper-proof
- No document scanning needed
- Fast and user-friendly
- Privacy-compliant
- Official UIDAI service

**Cons**: 
- Requires AUA/KUA registration
- Requires UIDAI API access
- Cost per verification (UIDAI charges)
- Requires internet connectivity
- Device must support camera

**Aadhaar Face RD Integration**:

```java
// Aadhaar Face RD Service Integration
@Service
public class AadhaarFaceRDService {
    
    private final String uidaiApiEndpoint = "https://auth.uidai.gov.in/faceauth";
    private final String auaCode; // Your AUA code from UIDAI
    private final String asaCode; // Your ASA code from UIDAI
    private final String licenseKey; // Your license key
    
    /**
     * Initiate Aadhaar Face Authentication
     */
    public AadhaarFaceAuthResponse initiateFaceAuth(
        String aadhaarNumber,
        String transactionId
    ) {
        // Step 1: Generate OTP request
        OTPRequest otpRequest = new OTPRequest(
            aadhaarNumber,
            transactionId,
            auaCode,
            asaCode
        );
        
        // Step 2: Send OTP to registered mobile
        OTPResponse otpResponse = uidaiClient.sendOTP(otpRequest);
        
        // Step 3: Return OTP reference ID
        return new AadhaarFaceAuthResponse(
            otpResponse.getOtpReferenceId(),
            transactionId,
            "OTP_SENT"
        );
    }
    
    /**
     * Verify OTP and perform face authentication
     */
    public AadhaarFaceAuthResult verifyFaceAuth(
        String aadhaarNumber,
        String otp,
        String otpReferenceId,
        String transactionId,
        byte[] faceImage // Live face capture from camera
    ) {
        // Step 1: Verify OTP
        OTPVerificationRequest otpVerify = new OTPVerificationRequest(
            aadhaarNumber,
            otp,
            otpReferenceId,
            transactionId
        );
        
        OTPVerificationResponse otpResult = uidaiClient.verifyOTP(otpVerify);
        
        if (!otpResult.isVerified()) {
            return new AadhaarFaceAuthResult(false, "OTP_VERIFICATION_FAILED", null);
        }
        
        // Step 2: Perform Face Authentication
        FaceAuthRequest faceAuthRequest = new FaceAuthRequest(
            aadhaarNumber,
            transactionId,
            faceImage, // Base64 encoded face image
            auaCode,
            asaCode,
            licenseKey
        );
        
        FaceAuthResponse faceAuthResponse = uidaiClient.authenticateFace(faceAuthRequest);
        
        // Step 3: If face auth successful, get demographic data (if authorized)
        if (faceAuthResponse.isAuthenticated()) {
            DemographicData demoData = uidaiClient.getDemographicData(
                aadhaarNumber,
                transactionId,
                faceAuthResponse.getAuthToken()
            );
            
            return new AadhaarFaceAuthResult(
                true,
                "AUTHENTICATION_SUCCESS",
                demoData // Contains DOB, Name, etc.
            );
        }
        
        return new AadhaarFaceAuthResult(
            false,
            faceAuthResponse.getErrorCode(),
            null
        );
    }
}

// Response Models
public class AadhaarFaceAuthResult {
    private boolean authenticated;
    private String status;
    private DemographicData demographicData;
    private double matchScore; // 0-100
    private LocalDateTime verifiedAt;
}

public class DemographicData {
    private String name;
    private LocalDate dateOfBirth;
    private String gender;
    private String address; // If authorized
    private String photo; // Base64 encoded
}
```

**UIDAI API Endpoints** (Reference):
```
POST /otp/generate
POST /otp/verify
POST /faceauth/authenticate
GET /ekyc/getDemographicData
```

**Aadhaar Face RD App Integration** (Android/iOS):
```java
// Using Aadhaar Face RD SDK
Intent intent = new Intent("in.gov.uidai.facerd.AUTHENTICATE");
intent.putExtra("aadhaarNumber", aadhaarNumber);
intent.putExtra("transactionId", transactionId);
intent.putExtra("purpose", "AGE_VERIFICATION");
startActivityForResult(intent, FACE_RD_REQUEST_CODE);

// Handle result
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == FACE_RD_REQUEST_CODE) {
        if (resultCode == RESULT_OK) {
            boolean authenticated = data.getBooleanExtra("authenticated", false);
            String dob = data.getStringExtra("dateOfBirth");
            double matchScore = data.getDoubleExtra("matchScore", 0.0);
            // Process verification result
        }
    }
}
```

**Alternative: Aadhaar Face RD Web API** (For web apps):
```java
// Direct API integration (if web-based)
POST https://auth.uidai.gov.in/faceauth/v2/authenticate
Headers:
  Authorization: Bearer {your-token}
  X-AUA-Code: {your-aua-code}
  X-ASA-Code: {your-asa-code}

Body:
{
    "uid": "123456789012",
    "txnId": "unique-transaction-id",
    "faceImage": "base64-encoded-face-image",
    "purpose": "AGE_VERIFICATION"
}

Response:
{
    "status": "success",
    "authenticated": true,
    "matchScore": 98.5,
    "dateOfBirth": "1995-01-15",
    "name": "John Doe",
    "referenceId": "uidai-ref-id"
}
```

### Method 3: Government ID Verification (Alternative)

**Flow**:
1. Customer uploads government ID (Aadhaar/PAN/DL)
2. System extracts DOB using OCR
3. System verifies ID authenticity (checksum, format)
4. Customer takes live selfie
5. System matches selfie with ID photo
6. Calculate age from DOB
7. If age >= minimum AND face match → Verified ✅

**Pros**: Good balance of security and ease
**Cons**: Requires OCR and face matching

### Method 4: Video KYC (Enhanced)

**Flow**:
1. Customer starts video call
2. Agent/System verifies:
   - Live person (not photo)
   - Shows ID document
   - Reads out DOB
3. System extracts DOB from ID
4. System verifies face match
5. Calculate age
6. If age >= minimum → Verified ✅

**Pros**: Very secure, human verification
**Cons**: Requires video infrastructure, slower

---

## 🎯 Recommended Approach: Hybrid with Aadhaar Face RD Priority

**Best Practice**: Use multiple methods with fallback, prioritizing Aadhaar Face RD

```
1. Try Aadhaar Face RD (if available) → Most secure, official UIDAI service ⭐
   ↓ (if fails or not available)
2. Try Photo Verification (ID + Selfie) → Fast and reliable
   ↓ (if fails)
3. Manual Review by Admin → Fallback
```

**Why Aadhaar Face RD First?**
- ✅ Official UIDAI service (government-verified)
- ✅ Most secure and tamper-proof
- ✅ No document scanning needed
- ✅ Fast and user-friendly
- ✅ Privacy-compliant
- ✅ Legal compliance for age-restricted items

---

## 📱 Updated API Endpoints

### Delivery Confirmation (Updated)

```java
// Agent confirms delivery (with flexible location)
POST /api/v1/delivery/{deliveryId}/confirm
Body: {
    "latitude": 28.6139,
    "longitude": 77.2090,
    "locationAccuracy": 5.2,
    "customerNotAvailable": false,
    "actualDeliveryAddress": "Near Metro Station" // Optional
}

// Customer confirms delivery
POST /api/v1/delivery/{deliveryId}/customer-confirm
Body: {
    "latitude": 28.6140,
    "longitude": 77.2091,
    "locationAccuracy": 8.5,
    "agentNotAvailable": false
}
```

### Age Verification Endpoints

```java
// Check if age verification required
GET /api/v1/delivery/{deliveryId}/age-verification-required
Response: {
    "required": true,
    "minimumAge": 21,
    "reason": "ALCOHOL_DELIVERY"
}

// Submit age verification (Photo method)
POST /api/v1/delivery/{deliveryId}/age-verify/photo
Body: {
    "customerPhoto": "base64-encoded-image",
    "idPhoto": "base64-encoded-image",
    "idType": "AADHAAR", // AADHAAR, PAN, DRIVING_LICENSE
    "idNumber": "1234 5678 9012"
}

// Submit age verification (Aadhaar Face RD) - Step 1: Generate OTP
POST /api/v1/delivery/{deliveryId}/age-verify/aadhaar-face-rd/initiate
Body: {
    "aadhaarNumber": "123456789012"
}
Response: {
    "otpReferenceId": "ref-12345",
    "transactionId": "txn-67890",
    "status": "OTP_SENT"
}

// Step 2: Verify OTP and Face Authentication
POST /api/v1/delivery/{deliveryId}/age-verify/aadhaar-face-rd/verify
Body: {
    "aadhaarNumber": "123456789012",
    "otp": "123456",
    "otpReferenceId": "ref-12345",
    "transactionId": "txn-67890",
    "faceImage": "base64-encoded-live-face-image" // From camera
}
Response: {
    "authenticated": true,
    "matchScore": 98.5,
    "dateOfBirth": "1995-01-15",
    "verifiedAge": 30,
    "status": "VERIFIED"
}

// Get age verification status
GET /api/v1/delivery/{deliveryId}/age-verification-status
Response: {
    "status": "VERIFIED", // PENDING, VERIFIED, FAILED, REJECTED
    "method": "AADHAAR_BIOMETRIC",
    "verifiedAge": 25,
    "minimumAge": 21,
    "verifiedAt": "2025-11-06T10:30:00Z",
    "confidence": 98.5
}
```

---

## 🔄 Updated Delivery Flow

### Complete Flow with Age Verification

```
1. Delivery assigned to agent
   ↓
2. Agent arrives at location (or customer location)
   ↓
3. Agent marks "Arrived" with GPS
   ↓
4. System checks:
   - Order contains restricted items?
   ↓
5. If YES → Age verification required
   ↓
6. Customer receives notification: "Age verification required"
   ↓
7. Customer chooses verification method:
   a. Aadhaar Biometric (recommended)
   b. Photo Verification (ID + Selfie)
   ↓
8. System verifies age
   ↓
9. If age verified ✅:
   - Allow delivery confirmation
   - Both parties confirm (with proximity check)
   - Mark as DELIVERED
   
   If age NOT verified ❌:
   - Block delivery
   - Return to warehouse
   - Notify customer and admin
```

---

## 🛡️ Security Enhancements

### Age Verification Security

1. **Aadhaar Data Protection**
   - Store only reference ID (not actual Aadhaar number)
   - Encrypt biometric data
   - Comply with UIDAI guidelines

2. **Photo Verification Security**
   - Verify photo is recent (EXIF data)
   - Detect if photo is screenshot/edited
   - Face liveness detection

3. **Fraud Prevention**
   - Rate limit verification attempts
   - Flag suspicious patterns
   - Manual review for edge cases

4. **Audit Trail**
   - Log all verification attempts
   - Store verification evidence
   - Track verification history

---

## 📊 Age Verification Status Flow

```
PENDING
  ↓
VERIFICATION_IN_PROGRESS
  ↓
VERIFIED ✅ → Allow delivery
  OR
FAILED ❌ → Block delivery, return
  OR
REJECTED ❌ → Manual review required
  OR
MANUAL_REVIEW → Admin intervention
```

---

## 🎯 Implementation Priority

### Phase 1: Flexible Location (Week 1)
- [ ] Update proximity logic (parties proximity, not address)
- [ ] Add actual delivery location fields
- [ ] Update confirmation flow

### Phase 2: Age Verification - Photo Method (Week 2)
- [ ] Create age_verifications table
- [ ] Photo upload endpoints
- [ ] OCR for ID extraction
- [ ] Face matching
- [ ] Age calculation

### Phase 3: Age Verification - Aadhaar Face RD (Week 3)
- [ ] Register as AUA/KUA with UIDAI
- [ ] Obtain UIDAI credentials and license
- [ ] Integrate Aadhaar Face RD SDK/API
- [ ] OTP generation and verification
- [ ] Face authentication flow
- [ ] Demographic data retrieval (DOB)
- [ ] Age calculation and verification

### Phase 4: Integration (Week 4)
- [ ] Integrate with delivery confirmation
- [ ] Block delivery if age not verified
- [ ] Return workflow for failed verification
- [ ] Admin dashboard

---

## 💡 Additional Ideas

### 1. Pre-Verification
- Verify age at order placement (for restricted items)
- Store verification status
- Re-verify at delivery (optional, for high-value items)

### 2. Trusted Customer Program
- Customers who verify once → Trusted status
- Skip verification for future orders (within validity period)
- Periodic re-verification

### 3. Smart Age Detection
- Use AI to estimate age from photo
- Use as preliminary check
- Still require official verification

### 4. QR Code Verification
- Generate QR code for delivery
- Customer scans QR code
- Links to age verification flow

---

## ✅ Benefits

### Flexible Location
- ✅ Customer convenience (meet anywhere)
- ✅ Reduces failed deliveries
- ✅ Better customer experience
- ✅ Still maintains security (proximity check)

### Age Verification
- ✅ Legal compliance
- ✅ Prevents underage sales
- ✅ Multiple verification methods
- ✅ Audit trail for compliance

---

*Last Updated: 2025-11-06*
*Design Version: 2.0 - With Flexible Location & Age Verification*

