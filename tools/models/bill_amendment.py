"""
Pydantic model for BillAmendment, translated from Java BillAmendment.
"""

from typing import Optional
from pydantic import BaseModel, validator
from .legislation_models import Version, BillId, BillTextFormat

class BillAmendment(BaseModel):
    bill_id: BillId
    version: Version
    data_source: str = "federal"  # Default for GovInfo
    version_id: Optional[str] = None
    memo: Optional[str] = None
    full_text: Optional[str] = None

    @validator('full_text')
    def validate_full_text(cls, v):
        if v and len(v) > 1000000:  # Reasonable limit
            raise ValueError('Full text too long')
        return v
