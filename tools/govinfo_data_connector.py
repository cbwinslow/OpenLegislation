#!/usr/bin/env python3
"""
GovInfo Data Connector - Universal Database Integration

Transforms govinfo bulk XML data into the unified OpenLegislation database schema.
Maps federal legislative data to work alongside existing state legislative data.

Usage:
    python govinfo_data_connector.py --input-dir /path/to/govinfo/xml --db-config config.json
"""

import argparse
import json
import logging
import os
import sys
import xml.etree.ElementTree as ET
from datetime import datetime
from pathlib import Path
from typing import Dict, List, Optional, Any
import psycopg2
import psycopg2.extras
from psycopg2 import sql
from tools.models.legislation_models import Bill, BillSponsor, BillAction, BillAmendment
from tools.models.bill_amendment import BillAmendment
from datetime import datetime
from typing import Optional, List

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class GovInfoDataConnector:
    """Data connector for transforming govinfo XML to unified database schema"""

    def __init__(self, db_config: Dict[str, Any]):
        self.db_config = db_config
        self.conn = None
        self.cursor = None

    def connect_db(self):
        """Establish database connection"""
        try:
            self.conn = psycopg2.connect(**self.db_config)
            self.cursor = self.conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor)
            logger.info("Database connection established")
        except Exception as e:
            logger.error(f"Failed to connect to database: {e}")
            raise

    def disconnect_db(self):
        """Close database connection"""
        if self.cursor:
            self.cursor.close()
        if self.conn:
            self.conn.close()
        logger.info("Database connection closed")

    def parse_govinfo_xml(self, xml_file: Path) -> Optional[Bill]:
        """Parse govinfo XML file and extract bill data into Pydantic Bill model"""
        try:
            tree = ET.parse(xml_file)
            root = tree.getroot()

            # Extract data
            congress = self._extract_congress(root)
            bill_number = self._extract_bill_number(root)
            bill_type_str = self._extract_bill_type(root)
            title = self._extract_title(root)
            short_title = self._extract_short_title(root)
            introduced_date = self._extract_introduced_date(root)
            sponsor_data = self._extract_sponsor(root)
            cosponsors_data = self._extract_cosponsors(root)
            actions_data = self._extract_actions(root)
            committees_data = self._extract_committees(root)
            subjects_data = self._extract_subjects(root)
            text_versions_data = self._extract_text_versions(root)

            if not congress or not bill_number:
                logger.warning(f"Missing essential data in {xml_file}")
                return None

            # Create BaseBillId
            base_bill_id = BaseBillId(
                session=congress,
                base_print_no=bill_number,
                bill_type=BillType(bill_type_str) if bill_type_str else BillType.HR
            )

            # Create Bill
            bill = Bill(
                base_bill_id=base_bill_id,
                title=title or "",
                summary=short_title or "",  # Use short_title as summary if no summary
                sponsor=BillSponsor(member=sponsor_data['name']) if sponsor_data else None,
                actions=[BillAction(
                    bill_id=BillId(session=congress, print_no=bill_number, version=Version.ORIGINAL),
                    date=introduced_date,
                    chamber=Chamber.HOUSE if 'H' in bill_number else Chamber.SENATE,
                    sequence_no=1,
                    text=actions_data[0]['description'] if actions_data else "Introduced"
                )] if actions_data else [],
                # For amendment, create a basic one with text
                amendment_map={
                    Version.ORIGINAL: BillAmendment(
                        bill_id=BillId(session=congress, print_no=bill_number, version=Version.ORIGINAL),
                        version=Version.ORIGINAL,
                        full_text=text_versions_data[0]['content'] if text_versions_data else ""
                    )
                },
                # Other fields default to empty
            )

            # Add cosponsors as additional_sponsors (simplified as str)
            bill.additional_sponsors = [s['name'] for s in cosponsors_data] if
    def _extract_title(self, root: ET.Element) -> Optional[str]:
        """Extract official title"""
        title_elem = root.find('.//official-title')
        return title_elem.text if title_elem is not None else None

    def _extract_short_title(self, root: ET.Element) -> Optional[str]:
        """Extract short title if present"""
        short_title_elem = root.find('.//short-title')
        return short_title_elem.text if short_title_elem is not None else None

    def _extract_introduced_date(self, root: ET.Element) -> Optional[datetime]:
        """Extract introduced date from action-date"""
        date_elem = root.find('.//action-date')
        if date_elem is not None and date_elem.text:
            try:
                # Format is YYYYMMDD
                date_str = date_elem.text
                if len(date_str) == 8:
                    return datetime.strptime(date_str, '%Y%m%d')
            except ValueError:
                logger.warning(f"Invalid date format: {date_elem.text}")
        return None

    def _extract_sponsor(self, root: ET.Element) -> Optional[Dict[str, Any]]:
        """Extract sponsor information from action sponsor"""
        sponsor_elem = root.find('.//sponsor')
        if sponsor_elem is not None:
            return {
                'name': sponsor_elem.text or sponsor_elem.get('name-id'),
                'party': None,  # Not directly available, may need deeper parsing
                'state': None,
                'district': None
            }
        return None

    def _extract_cosponsors(self, root: ET.Element) -> List[Dict[str, Any]]:
        """Extract cosponsors from action cosponsors"""
        cosponsors = []
        for cosponsor_elem in root.findall('.//cosponsor'):
            cosponsor = {
                'name': cosponsor_elem.text or cosponsor_elem.get('name-id'),
                'party': None,
                'state': None,
                'district': None,
                'sponsor_type': 'cosponsor',
                'date_added': None  # Not in sample
            }
            cosponsors.append(cosponsor)
        return cosponsors

    def _extract_actions(self, root: ET.Element) -> List[Dict[str, Any]]:
        """Extract actions from form action desc - basic for now"""
        actions = []
        action_elem = root.find('.//action')
        if action_elem is not None:
            action = {
                'action_date': self._extract_date(action_elem, './/action-date'),
                'chamber': 'House',  # From current-chamber or infer
                'description': action_elem.find('.//action-desc').text if action_elem.find('.//action-desc') is not None else '',
                'action_type': 'Introduced',  # Parse from desc
                'sequence_no': 1
            }
            actions.append(action)
        return actions

    def _extract_committees(self, root: ET.Element) -> List[Dict[str, Any]]:
        """Extract committees from action committee-name"""
        committees = []
        for comm_elem in root.findall('.//committee-name'):
            committee = {
                'name': comm_elem.text,
                'referred_date': self._extract_date(root, './/action-date')  # Same date as intro for now
            }
            committees.append(committee)
        return committees

    def _extract_subjects(self, root: ET.Element) -> List[str]:
        """Extract subjects if present - sample has TOC, but no explicit subjects"""
        # In sample, subjects might be in subjects section, but not obvious; placeholder
        subjects_elem = root.find('.//subjects')
        if subjects_elem is not None:
            return [s.text for s in subjects_elem.findall('.//subject') if s.text]
        return []

    def _extract_text_versions(self, root: ET.Element) -> List[Dict[str, Any]]:
        """Extract bill text from legis-body"""
        versions = []
        legis_body = root.find('.//legis-body')
        if legis_body is not None:
            content = ET.tostring(legis_body, encoding='unicode', method='text')
            versions.append({
                'version_id': 'Introduced',
                'format': 'xml',
                'content': ET.tostring(root, encoding='unicode')  # Full text for now
            })
        return versions

    def _extract_relationships(self, root: ET.Element) -> List[Dict[str, Any]]:
        """Extract relationships if present - not in basic sample"""
        return []

    def _extract_text(self, element: ET.Element, xpath: str) -> Optional[str]:
        """Extract text content from XML element"""
        found = element.find(xpath)
        return found.text if found is not None else None

    def _extract_date(self, element: ET.Element, xpath: str) -> Optional[datetime]:
        """Extract and parse date from XML element"""
        text = self._extract_text(element, xpath)
        if text:
            try:
                return datetime.fromisoformat(text.replace('Z', '+00:00'))
            except ValueError:
                logger.warning(f"Invalid date format: {text}")
        return None

    def _extract_int(self, element: ET.Element, xpath: str) -> Optional[int]:
        """Extract and parse integer from XML element"""
        text = self._extract_text(element, xpath)
        if text:
            try:
                return int(text)
            except ValueError:
                logger.warning(f"Invalid integer format: {text}")
        return None

    def insert_bill_data(self, bill_data: Dict[str, Any]) -> bool:
        """Insert transformed bill data into unified database schema"""
        try:
            # Start transaction
            self.conn.autocommit = False

            # Insert main bill record
            bill_id = self._insert_main_bill(bill_data)

            if bill_id:
                # Insert related data
                self._insert_sponsor(bill_data, bill_id)
                self._insert_cosponsors(bill_data, bill_id)
                self._insert_actions(bill_data, bill_id)
                self._insert_committees(bill_data, bill_id)
                self._insert_subjects(bill_data, bill_id)
                self._insert_text_versions(bill_data, bill_id)
                self._insert_relationships(bill_data, bill_id)

                self.conn.commit()
                logger.info(f"Successfully inserted bill {bill_data.get('bill_number')}")
                return True
            else:
                self.conn.rollback()
                return False

        except Exception as e:
            self.conn.rollback()
            logger.error(f"Failed to insert bill data: {e}")
            return False
        finally:
            self.conn.autocommit = True

    def _insert_main_bill(self, bill_data: Dict[str, Any]) -> Optional[int]:
        """Insert main bill record"""
        query = """
            INSERT INTO master.bill (
                bill_print_no, bill_session_year, title, data_source, congress,
                bill_type, short_title, status_date, sponsor_party, sponsor_state, sponsor_district
            ) VALUES (%s, %s, %s, 'federal', %s, %s, %s, %s, %s, %s, %s)
            ON CONFLICT (bill_print_no, bill_session_year)
            DO UPDATE SET
                title = EXCLUDED.title,
                congress = EXCLUDED.congress,
                bill_type = EXCLUDED.bill_type,
                short_title = EXCLUDED.short_title,
                status_date = EXCLUDED.status_date,
                sponsor_party = EXCLUDED.sponsor_party,
                sponsor_state = EXCLUDED.sponsor_state,
                sponsor_district = EXCLUDED.sponsor_district
            RETURNING bill_print_no, bill_session_year
        """

        sponsor = bill_data.get('sponsor', {})
        introduced_date = bill_data.get('introduced_date')

        self.cursor.execute(query, (
            bill_data.get('bill_number'),
            bill_data.get('congress'),  # Use congress as session year for federal
            bill_data.get('title'),
            bill_data.get('congress'),
            bill_data.get('bill_type'),
            bill_data.get('short_title'),
            introduced_date,
            sponsor.get('party'),
            sponsor.get('state'),
            sponsor.get('district')
        ))

        result = self.cursor.fetchone()
        return result['bill_print_no'] if result else None

    def _insert_sponsor(self, bill_data: Dict[str, Any], bill_id: str):
        """Insert bill sponsor"""
        sponsor = bill_data.get('sponsor')
        if sponsor and sponsor.get('name'):
            query = """
                INSERT INTO master.bill_sponsor (
                    bill_print_no, bill_session_year, session_member_id,
                    data_source, sponsor_type
                ) VALUES (%s, %s, %s, 'federal', 'sponsor')
                ON CONFLICT (bill_print_no, bill_session_year) DO NOTHING
            """

            # For federal data, we'll use the sponsor name as a placeholder
            # In a full implementation, this would map to actual member IDs
            self.cursor.execute(query, (
                bill_id,
                bill_data.get('congress'),
                sponsor.get('name')  # Placeholder - would need member mapping
            ))

    def _insert_cosponsors(self, bill_data: Dict[str, Any], bill_id: str):
        """Insert cosponsors"""
        cosponsors = bill_data.get('cosponsors', [])
        for cosponsor in cosponsors:
            if cosponsor.get('name'):
                query = """
                    INSERT INTO master.bill_amendment_cosponsor (
                        bill_print_no, bill_session_year, bill_amend_version,
                        session_member_id, data_source, sponsor_type
                    ) VALUES (%s, %s, '', %s, 'federal', 'cosponsor')
                """

                self.cursor.execute(query, (
                    bill_id,
                    bill_data.get('congress'),
                    cosponsor.get('name')  # Placeholder - would need member mapping
                ))

    def _insert_actions(self, bill_data: Dict[str, Any], bill_id: str):
        """Insert bill actions"""
        actions = bill_data.get('actions', [])
        for action in actions:
            query = """
                INSERT INTO master.bill_amendment_action (
                    bill_print_no, bill_session_year, bill_amend_version,
                    effect_date, chamber, text, data_source, action_type, sequence_no
                ) VALUES (%s, %s, '', %s, %s, %s, 'federal', %s, %s)
            """

            self.cursor.execute(query, (
                bill_id,
                bill_data.get('congress'),
                action.get('action_date'),
                action.get('chamber'),
                action.get('description'),
                action.get('action_type'),
                action.get('sequence_no')
            ))

    def _insert_committees(self, bill_data: Dict[str, Any], bill_id: str):
        """Insert committee references"""
        committees = bill_data.get('committees', [])
        for committee in committees:
            if committee.get('name'):
                query = """
                    INSERT INTO master.bill_committee (
                        bill_print_no, bill_session_year, committee_name,
                        action_date, data_source
                    ) VALUES (%s, %s, %s, %s, 'federal')
                """

                self.cursor.execute(query, (
                    bill_id,
                    bill_data.get('congress'),
                    committee.get('name'),
                    committee.get('referred_date')
                ))

    def _insert_subjects(self, bill_data: Dict[str, Any], bill_id: str):
        """Insert legislative subjects"""
        subjects = bill_data.get('subjects', [])
        for subject in subjects:
            query = """
                INSERT INTO master.federal_bill_subject (
                    bill_print_no, bill_session_year, subject
                ) VALUES (%s, %s, %s)
            """

            self.cursor.execute(query, (
                bill_id,
                bill_data.get('congress'),
                subject
            ))

    def _insert_text_versions(self, bill_data: Dict[str, Any], bill_id: str):
        """Insert text versions"""
        versions = bill_data.get('text_versions', [])
        for version in versions:
            if version.get('content'):
                query = """
                    INSERT INTO master.federal_bill_text (
                        bill_print_no, bill_session_year, bill_amend_version,
                        version_id, text_format, content
                    ) VALUES (%s, %s, '', %s, %s, %s)
                """

                self.cursor.execute(query, (
                    bill_id,
                    bill_data.get('congress'),
                    version.get('version_id'),
                    version.get('format'),
                    version.get('content')
                ))

    def _insert_relationships(self, bill_data: Dict[str, Any], bill_id: str):
        """Insert bill relationships"""
        relationships = bill_data.get('relationships', [])
        for rel in relationships:
            if rel.get('related_bill'):
                query = """
                    INSERT INTO master.federal_bill_relationship (
                        bill_print_no, bill_session_year, related_bill_print_no,
                        related_session_year, relationship_type
                    ) VALUES (%s, %s, %s, %s, %s)
                """

                self.cursor.execute(query, (
                    bill_id,
                    bill_data.get('congress'),
                    rel.get('related_bill'),
                    bill_data.get('congress'),  # Assume same congress
                    rel.get('type', 'related')
                ))

    def process_directory(self, input_dir: Path, batch_size: int = 100, continue_on_error: bool = True):
        """Process all XML files in directory with batching and error handling"""
        xml_files = list(input_dir.glob('**/*.xml'))
        logger.info(f"Found {len(xml_files)} XML files to process")

        processed = 0
        errors = 0
        batch = []

        for xml_file in xml_files:
            try:
                bill_data = self.parse_govinfo_xml(xml_file)
                if bill_data:
                    batch.append(bill_data)
                    if len(batch) >= batch_size:
                        success_count = self._process_batch(batch)
                        processed += success_count
                        errors += len(batch) - success_count
                        batch = []
                        logger.info(f"Progress: {processed + errors}/{len(xml_files)} processed")
                else:
                    errors += 1
            except Exception as e:
                logger.error(f"Failed to process {xml_file}: {e}")
                if not continue_on_error:
                    raise
                errors += 1

        # Process remaining batch
        if batch:
            success_count = self._process_batch(batch)
            processed += success_count
            errors += len(batch) - success_count

        logger.info(f"Processing complete: {processed} successful, {errors} errors")

    def _process_batch(self, batch: List[Dict[str, Any]]) -> int:
        """Process a batch of bill data"""
        success_count = 0
        for bill_data in batch:
            if self.insert_bill_data(bill_data):
                success_count += 1
        return success_count


def load_db_config(config_file: Path) -> Dict[str, Any]:
    """Load database configuration from JSON file"""
    with open(config_file, 'r') as f:
        config = json.load(f)

    # Ensure required fields
    required_fields = ['host', 'database', 'user', 'password']
    for field in required_fields:
        if field not in config:
            raise ValueError(f"Missing required field '{field}' in config file")

    return config


def main():
    parser = argparse.ArgumentParser(description='GovInfo Data Connector')
    parser.add_argument('--input-dir', required=True, help='Directory containing govinfo XML files')
    parser.add_argument('--db-config', required=True, help='Database configuration JSON file')
    parser.add_argument('--batch-size', type=int, default=100, help='Batch size for processing')
    parser.add_argument('--continue-on-error', action='store_true', default=True, help='Continue processing on errors')
    parser.add_argument('--log-level', default='INFO', choices=['DEBUG', 'INFO', 'WARNING', 'ERROR'])

    args = parser.parse_args()

    # Set log level
    logging.getLogger().setLevel(getattr(logging, args.log_level))

    # Validate inputs
    input_dir = Path(args.input_dir)
    if not input_dir.exists() or not input_dir.is_dir():
        logger.error(f"Input directory does not exist: {input_dir}")
        sys.exit(1)

    config_file = Path(args.db_config)
    if not config_file.exists():
        logger.error(f"Config file does not exist: {config_file}")
        sys.exit(1)

    # Load configuration
    try:
        db_config = load_db_config(config_file)
    except Exception as e:
        logger.error(f"Failed to load config: {e}")
        sys.exit(1)

    # Process data
    connector = GovInfoDataConnector(db_config)
    try:
        connector.connect_db()
        connector.process_directory(input_dir, args.batch_size, args.continue_on_error)
    except Exception as e:
        logger.error(f"Processing failed: {e}")
        sys.exit(1)
    finally:
        connector.disconnect_db()


if __name__ == '__main__':
    main()