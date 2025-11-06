# Aadhaar Face RD Integration Guide

## 📱 What is Aadhaar Face RD?

**Aadhaar Face RD (Registered Device)** is UIDAI's official mobile application that enables face authentication using Aadhaar biometric data. It's now available for private entities to integrate into their applications.

## 🎯 Key Benefits

1. ✅ **Official UIDAI Service**: Government-verified and secure
2. ✅ **No Physical Documents**: Eliminates need for ID cards
3. ✅ **Fast & Seamless**: Instant verification
4. ✅ **Privacy-First**: User controls data sharing
5. ✅ **Tamper-Proof**: Cannot be manipulated
6. ✅ **Legal Compliance**: Meets regulatory requirements

## 📋 Prerequisites

### 1. Register with UIDAI

**Steps**:
1. Visit **Aadhaar Good Governance Portal**: https://goodgovernance.uidai.gov.in
2. Register your organization as:
   - **AUA (Authentication User Agency)** - For authentication services
   - **KUA (KYC User Agency)** - For KYC services
3. Submit required documents:
   - Company registration certificate
   - GST certificate
   - Bank details
   - Technical architecture document
4. Wait for UIDAI approval (typically 2-4 weeks)
5. Receive credentials:
   - AUA Code
   - ASA Code (Authentication Service Agency)
   - License Key
   - API credentials

### 2. Technical Requirements

- **Backend**: REST API integration capability
- **Mobile App**: Android/iOS with camera support
- **Network**: Internet connectivity required
- **Compliance**: Aadhaar Act compliance

## 🔧 Integration Methods

### Method 1: Aadhaar Face RD App Integration (Recommended for Mobile)

**For Android/iOS Native Apps**

```java
// Android Integration
public class AadhaarFaceRDActivity extends AppCompatActivity {
    
    private static final int FACE_RD_REQUEST_CODE = 1001;
    
    public void initiateAadhaarFaceAuth(String aadhaarNumber) {
        Intent intent = new Intent("in.gov.uidai.facerd.AUTHENTICATE");
        intent.setPackage("in.gov.uidai.facerd");
        intent.putExtra("aadhaarNumber", aadhaarNumber);
        intent.putExtra("transactionId", generateTransactionId());
        intent.putExtra("purpose", "AGE_VERIFICATION");
        intent.putExtra("auaCode", getAUACode());
        intent.putExtra("asaCode", getASACode());
        
        try {
            startActivityForResult(intent, FACE_RD_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            // Aadhaar Face RD app not installed
            // Prompt user to install from Play Store
            promptInstallAadhaarFaceRD();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == FACE_RD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                boolean authenticated = data.getBooleanExtra("authenticated", false);
                String dob = data.getStringExtra("dateOfBirth");
                String name = data.getStringExtra("name");
                double matchScore = data.getDoubleExtra("matchScore", 0.0);
                String referenceId = data.getStringExtra("referenceId");
                
                if (authenticated && matchScore >= 80.0) {
                    // Age verification successful
                    processAgeVerification(dob, name, matchScore);
                } else {
                    // Authentication failed
                    handleAuthenticationFailure();
                }
            } else {
                // User cancelled or error
                handleCancellation();
            }
        }
    }
}
```

**For iOS**:
```swift
// iOS Integration
import AadhaarFaceRD

func initiateAadhaarFaceAuth(aadhaarNumber: String) {
    let faceRD = AadhaarFaceRD()
    faceRD.aadhaarNumber = aadhaarNumber
    faceRD.transactionId = generateTransactionId()
    faceRD.purpose = "AGE_VERIFICATION"
    faceRD.auaCode = getAUACode()
    faceRD.asaCode = getASACode()
    
    faceRD.authenticate { result in
        switch result {
        case .success(let authResult):
            if authResult.authenticated && authResult.matchScore >= 80.0 {
                processAgeVerification(
                    dob: authResult.dateOfBirth,
                    name: authResult.name,
                    matchScore: authResult.matchScore
                )
            }
        case .failure(let error):
            handleAuthenticationFailure(error: error)
        }
    }
}
```

### Method 2: Direct API Integration (For Web/Backend)

**UIDAI Face Authentication API**

```java
@Service
public class AadhaarFaceRDServiceImpl implements AadhaarFaceRDService {
    
    @Value("${uidai.api.endpoint}")
    private String uidaiApiEndpoint;
    
    @Value("${uidai.aua.code}")
    private String auaCode;
    
    @Value("${uidai.asa.code}")
    private String asaCode;
    
    @Value("${uidai.license.key}")
    private String licenseKey;
    
    private final RestTemplate restTemplate;
    
    @Override
    public AadhaarOTPResponse initiateFaceAuth(String aadhaarNumber) {
        // Step 1: Generate OTP
        OTPRequest request = new OTPRequest(
            aadhaarNumber,
            generateTransactionId(),
            auaCode,
            asaCode
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-AUA-Code", auaCode);
        headers.set("X-ASA-Code", asaCode);
        headers.set("X-License-Key", licenseKey);
        
        HttpEntity<OTPRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<OTPResponse> response = restTemplate.postForEntity(
            uidaiApiEndpoint + "/otp/generate",
            entity,
            OTPResponse.class
        );
        
        if (response.getStatusCode().is2xxSuccessful() && 
            response.getBody() != null) {
            OTPResponse otpResponse = response.getBody();
            return new AadhaarOTPResponse(
                otpResponse.getOtpReferenceId(),
                request.getTransactionId(),
                "OTP_SENT",
                null
            );
        }
        
        return new AadhaarOTPResponse(
            null, null, "FAILED", 
            "Failed to generate OTP"
        );
    }
    
    @Override
    public AadhaarFaceAuthResult verifyFaceAuth(
        String aadhaarNumber,
        String otp,
        String otpReferenceId,
        String transactionId,
        String faceImage
    ) {
        // Step 1: Verify OTP
        OTPVerificationRequest otpRequest = new OTPVerificationRequest(
            aadhaarNumber,
            otp,
            otpReferenceId,
            transactionId
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-AUA-Code", auaCode);
        headers.set("X-ASA-Code", asaCode);
        
        HttpEntity<OTPVerificationRequest> otpEntity = 
            new HttpEntity<>(otpRequest, headers);
        
        ResponseEntity<OTPVerificationResponse> otpResponse = 
            restTemplate.postForEntity(
                uidaiApiEndpoint + "/otp/verify",
                otpEntity,
                OTPVerificationResponse.class
            );
        
        if (!otpResponse.getBody().isVerified()) {
            return new AadhaarFaceAuthResult(
                false, "OTP_VERIFICATION_FAILED", 
                "INVALID_OTP", "OTP verification failed",
                0.0, null, null
            );
        }
        
        // Step 2: Perform Face Authentication
        FaceAuthRequest faceRequest = new FaceAuthRequest(
            aadhaarNumber,
            transactionId,
            faceImage, // Base64 encoded
            auaCode,
            asaCode
        );
        
        HttpEntity<FaceAuthRequest> faceEntity = 
            new HttpEntity<>(faceRequest, headers);
        
        ResponseEntity<FaceAuthResponse> faceResponse = 
            restTemplate.postForEntity(
                uidaiApiEndpoint + "/faceauth/v2/authenticate",
                faceEntity,
                FaceAuthResponse.class
            );
        
        FaceAuthResponse authResponse = faceResponse.getBody();
        
        if (authResponse != null && authResponse.isAuthenticated()) {
            // Step 3: Get Demographic Data (if authorized)
            DemographicDataRequest demoRequest = new DemographicDataRequest(
                aadhaarNumber,
                transactionId,
                authResponse.getAuthToken()
            );
            
            HttpEntity<DemographicDataRequest> demoEntity = 
                new HttpEntity<>(demoRequest, headers);
            
            ResponseEntity<DemographicDataResponse> demoResponse = 
                restTemplate.postForEntity(
                    uidaiApiEndpoint + "/ekyc/getDemographicData",
                    demoEntity,
                    DemographicDataResponse.class
                );
            
            DemographicDataResponse demo = demoResponse.getBody();
            
            if (demo != null) {
                DemographicData data = new DemographicData(
                    demo.getName(),
                    LocalDate.parse(demo.getDateOfBirth()),
                    demo.getGender(),
                    demo.getAddress(),
                    demo.getPhoto(),
                    authResponse.getReferenceId()
                );
                
                return new AadhaarFaceAuthResult(
                    true,
                    "AUTHENTICATION_SUCCESS",
                    null,
                    null,
                    authResponse.getMatchScore(),
                    data,
                    LocalDateTime.now()
                );
            }
        }
        
        return new AadhaarFaceAuthResult(
            false,
            "AUTHENTICATION_FAILED",
            authResponse != null ? authResponse.getErrorCode() : "UNKNOWN",
            authResponse != null ? authResponse.getErrorMessage() : "Authentication failed",
            0.0,
            null,
            null
        );
    }
    
    @Override
    public int calculateAge(LocalDate dateOfBirth) {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
    
    @Override
    public boolean verifyAge(int age, int minimumAge) {
        return age >= minimumAge;
    }
}
```

## 📝 API Request/Response Models

```java
// OTP Generation Request
public class OTPRequest {
    private String uid; // Aadhaar number
    private String txnId; // Transaction ID
    private String auaCode;
    private String asaCode;
}

// OTP Generation Response
public class OTPResponse {
    private String otpReferenceId;
    private String status; // SUCCESS, FAILED
    private String errorCode;
}

// Face Authentication Request
public class FaceAuthRequest {
    private String uid;
    private String txnId;
    private String faceImage; // Base64 encoded
    private String auaCode;
    private String asaCode;
}

// Face Authentication Response
public class FaceAuthResponse {
    private boolean authenticated;
    private double matchScore; // 0-100
    private String referenceId;
    private String errorCode;
    private String errorMessage;
    private String authToken; // For getting demographic data
}
```

## 🔒 Security & Compliance

### 1. Data Protection
- ✅ Store only reference IDs (not Aadhaar numbers)
- ✅ Encrypt all API communications (HTTPS)
- ✅ Comply with Aadhaar Act and IT Act
- ✅ Implement data retention policies

### 2. User Consent
- ✅ Explicit consent before authentication
- ✅ Clear purpose declaration
- ✅ Privacy policy compliance

### 3. Audit Trail
- ✅ Log all authentication attempts
- ✅ Store reference IDs
- ✅ Track success/failure rates

## 💰 Cost Considerations

- **UIDAI Charges**: Per authentication (check current rates)
- **API Integration**: Development cost
- **Infrastructure**: Server costs for API calls

## 🚀 Implementation Checklist

- [ ] Register as AUA/KUA with UIDAI
- [ ] Obtain credentials and license
- [ ] Set up UIDAI API integration
- [ ] Implement OTP generation
- [ ] Implement OTP verification
- [ ] Implement face authentication
- [ ] Implement demographic data retrieval
- [ ] Add age calculation logic
- [ ] Add error handling
- [ ] Add logging and audit trail
- [ ] Test with UIDAI test environment
- [ ] Get production approval
- [ ] Deploy to production

## 📚 Resources

- **UIDAI Official Site**: https://uidai.gov.in
- **Aadhaar Good Governance Portal**: https://goodgovernance.uidai.gov.in
- **Aadhaar Face RD App**: Available on Play Store
- **API Documentation**: Provided by UIDAI after registration

---

*Last Updated: 2025-11-06*
*Integration Guide Version: 1.0*

