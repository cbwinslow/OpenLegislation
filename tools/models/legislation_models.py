"""
Pydantic models for OpenLegislation domain entities.

These models translate Java classes from src/main/java/gov/nysenate/openleg/legislation/bill/
to Python Pydantic models for validation and serialization in the GovInfo ingestion pipeline.
"""

from datetime import datetime
from typing import Optional, List, Set, Dict, Any
from enum import Enum
from pydantic import BaseModel, validator
from gov.nysenate.openleg.legislation.bill import Chamber  # Assuming we import or define enums

class Version(str, Enum):
    ORIGINAL = "ORIGINAL"
    A = "A"
    B = "B"
    # Add other versions as needed

class PublishStatus(Enum):
    PUBLISHED = "PUBLISHED"
    UNPUBLISHED = "UNPUBLISHED"
    UNKNOWN = "UNKNOWN"

class Chamber(str, Enum):
    ASSEMBLY = "ASSEMBLY"
    SENATE = "SENATE"

class BillType(str, Enum):
    # Define bill types like HR, S, etc. for federal mapping
    HR = "HR"
    S = "S"
    HJR = "HJR"
    SJR = "SJR"
    # Add more as needed

class BaseBillId(BaseModel):
    session: int
    base_print_no: str
    bill_type: BillType

    @validator('session', pre=True)
    def validate_session(cls, v):
        if not isinstance(v, int) or v < 0:
            raise ValueError('Session must be a positive integer')
        return v

class BillId(BaseModel):
    session: int
    print_no: str
    version: Version = Version.ORIGINAL

    class Config:
        arbitrary_types_allowed = True

    def __init__(self, **data):
        super().__init__(**data)
        self.base_bill_id = BaseBillId(session=self.session, base_print_no=self.print_no, bill_type=BillType(self.print_no[:2]))  # Simplified

class BillSponsor(BaseModel):
    member: Optional[str] = None  # SessionMember simplified to str for now
    budget: bool = False
    rules: bool = False
    redistricting: bool = False

    @validator('member')
    def validate_member(cls, v):
        if v and not isinstance(v, str):
            raise ValueError('Member should be a string')
        return v

class BillAction(BaseModel):
    bill_id: BillId
    date: Optional[datetime] = None
    chamber: Optional[Chamber] = None
    sequence_no: int = 0
    text: str = ""

    @validator('sequence_no')
    def validate_sequence_no(cls, v):
        if not isinstance(v, int) or v < 0:
            raise ValueError('Sequence number must be non-negative')
        return v

class Bill(BaseModel):
    base_bill_id: BaseBillId
    title: str = ""
    summary: str = ""
    status: Optional[str] = None  # BillStatus simplified
    ldblurb: str = ""
    milestones: List[Any] = []  # BillStatus list, simplified
    amendment_map: Dict[Version, Any] = {}  # BillAmendment, to be defined
    amend_publish_status_map: Dict[Version, PublishStatus] = {}
    veto_messages: Dict[Any, Any] = {}  # VetoId -> VetoMessage
    approval_message: Optional[Any] = None  # ApprovalMessage
    active_version: Version = Version.ORIGINAL
    sponsor: Optional[BillSponsor] = None
    additional_sponsors: List[Any] = []  # SessionMember list
    past_committees: Set[Any] = set()  # CommitteeVersionId set
    actions: List[BillAction] = []
    substituted_by: Optional[BaseBillId] = None
    reprint_of: Optional[BillId] = None
    direct_previous_version: Optional[BillId] = None
    all_previous_versions: Set[BillId] = set()
    program_info: Optional[Any] = None  # ProgramInfo
    committee_agendas: List[Any] = []  # CommitteeAgendaId list
    calendars: List[Any] = []  # CalendarId list
    chapter_num: Optional[int] = None
    chapter_year: Optional[int] = None

    @validator('title')
    def validate_title(cls, v):
        if not v or len(v.strip()) == 0:
            raise ValueError('Title cannot be empty')
        return v.strip()

    def get_amendment(self, version: Version) -> Optional[Any]:
        # Simplified - in full impl, return BillAmendment
        return self.amendment_map.get(version)

    def add_action(self, action: BillAction):
        self.actions.append(action)

    # Add other methods as needed, like has_amendment, etc.

# Additional models can be added here for completeness
# For now, focusing on core Bill, BillSponsor, BillAction
