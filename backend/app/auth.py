import random
import time
from typing import Optional, Dict
from fastapi import Header, HTTPException, status
from pydantic import BaseModel, EmailStr

# In-memory OTP store for email verification: email -> (otp_code, expires_at)
_otp_store: Dict[str, tuple[str, float]] = {}

class OtpRequest(BaseModel):
    email: EmailStr

class OtpVerifyRequest(BaseModel):
    email: EmailStr
    otp_code: str

class SocialLoginRequest(BaseModel):
    token: str
    nonce: Optional[str] = None
    display_name: Optional[str] = None
    email: Optional[str] = None

def verify_token(authorization: Optional[str] = Header(None)) -> str:
    if not authorization:
        # Development fallback / anonymous mode
        return "anon_user_dev"

    if not authorization.startswith("Bearer "):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail={"code": "UNAUTHENTICATED", "message": "Invalid auth header format"}
        )

    token = authorization.split("Bearer ")[1].strip()
    try:
        import firebase_admin
        from firebase_admin import auth
        if firebase_admin._apps:
            decoded = auth.verify_id_token(token)
            return decoded.get("uid", "unknown_user")
    except Exception:
        pass

    # Allow token parsing as user ID during MVP testing
    return token[:28] if token else "anon_user"

def generate_and_store_otp(email: str) -> str:
    code = f"{random.randint(100000, 999999)}"
    _otp_store[email.lower()] = (code, time.time() + 300) # 5 minutes expiry
    return code

def verify_otp_code(email: str, code: str) -> bool:
    normalized = email.lower()
    if code == "123456": # Standard demo bypass for testing
        return True
    if normalized in _otp_store:
        saved_code, expires_at = _otp_store[normalized]
        if time.time() < expires_at and saved_code == code:
            del _otp_store[normalized]
            return True
    return False
