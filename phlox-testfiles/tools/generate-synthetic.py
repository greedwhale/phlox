#!/usr/bin/env python3
#
# Copyright (C) 2026 Philip Helger (www.helger.com)
# philip[at]helger[dot]com
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

"""
Generates the synthetic UBL 2.1 invoices in
src/main/resources/external/en16931/synthetic/ubl/invoice/ . They cover the cases that the imported
EN 16931 examples do not: an invoice of many lines (pagination), a text in Cyrillic and Greek
(fonts) and an invoice in a currency without decimals (JPY).

Usage: python3 tools/generate-synthetic.py   (from the phlox-testfiles directory)

The totals are calculated with decimal arithmetic, so the files are consistent. They are test data
for rendering: phlox does not validate business rules.
"""
import os
from decimal import Decimal, ROUND_HALF_UP
from xml.sax.saxutils import escape

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources", "external",
                   "en16931", "synthetic", "ubl", "invoice")


def q(value, digits):
    return value.quantize(Decimal(1).scaleb(-digits), rounding=ROUND_HALF_UP)


def fmt(value, digits):
    return str(q(value, digits))


def party(tag, name, legal_name, street, city, zip_code, country, vat, reg_id, endpoint, contact=None):
    c = ""
    if contact:
        c = f"""
      <cac:Contact>
        <cbc:Name>{escape(contact[0])}</cbc:Name>
        <cbc:Telephone>{contact[1]}</cbc:Telephone>
        <cbc:ElectronicMail>{contact[2]}</cbc:ElectronicMail>
      </cac:Contact>"""
    return f"""  <cac:{tag}>
    <cac:Party>
      <cbc:EndpointID schemeID="0088">{endpoint}</cbc:EndpointID>
      <cac:PartyName>
        <cbc:Name>{escape(name)}</cbc:Name>
      </cac:PartyName>
      <cac:PostalAddress>
        <cbc:StreetName>{escape(street)}</cbc:StreetName>
        <cbc:CityName>{escape(city)}</cbc:CityName>
        <cbc:PostalZone>{zip_code}</cbc:PostalZone>
        <cac:Country>
          <cbc:IdentificationCode>{country}</cbc:IdentificationCode>
        </cac:Country>
      </cac:PostalAddress>
      <cac:PartyTaxScheme>
        <cbc:CompanyID>{vat}</cbc:CompanyID>
        <cac:TaxScheme>
          <cbc:ID>VAT</cbc:ID>
        </cac:TaxScheme>
      </cac:PartyTaxScheme>
      <cac:PartyLegalEntity>
        <cbc:RegistrationName>{escape(legal_name)}</cbc:RegistrationName>
        <cbc:CompanyID>{reg_id}</cbc:CompanyID>
      </cac:PartyLegalEntity>{c}
    </cac:Party>
  </cac:{tag}>
"""


def tax_category(cat, percent, reason_code=None, reason=None):
    r = ""
    if reason_code:
        r += f"\n          <cbc:TaxExemptionReasonCode>{reason_code}</cbc:TaxExemptionReasonCode>"
    if reason:
        r += f"\n          <cbc:TaxExemptionReason>{escape(reason)}</cbc:TaxExemptionReason>"
    return f"""<cac:TaxCategory>
          <cbc:ID>{cat}</cbc:ID>
          <cbc:Percent>{percent}</cbc:Percent>{r}
          <cac:TaxScheme>
            <cbc:ID>VAT</cbc:ID>
          </cac:TaxScheme>
        </cac:TaxCategory>"""


def build(inv_id, issue, due, currency, digits, notes, supplier, customer, payment, terms, lines, cats):
    """lines: (name, description, qty, unit, price, key); cats: key -> (category code, percent, reason code, reason)"""
    out = []
    nets = {}
    total_net = Decimal(0)
    for i, (name, desc, qty, unit, price, cat) in enumerate(lines, 1):
        net = q(qty * price, digits)
        nets[cat] = nets.get(cat, Decimal(0)) + net
        total_net += net
        code, pct, rc, rs = cats[cat]
        d = f"\n      <cbc:Description>{escape(desc)}</cbc:Description>" if desc else ""
        out.append(f"""  <cac:InvoiceLine>
    <cbc:ID>{i}</cbc:ID>
    <cbc:InvoicedQuantity unitCode="{unit}">{qty}</cbc:InvoicedQuantity>
    <cbc:LineExtensionAmount currencyID="{currency}">{fmt(net, digits)}</cbc:LineExtensionAmount>
    <cac:Item>{d}
      <cbc:Name>{escape(name)}</cbc:Name>
      <cac:SellersItemIdentification>
        <cbc:ID>ART-{i:04d}</cbc:ID>
      </cac:SellersItemIdentification>
      <cac:ClassifiedTaxCategory>
        <cbc:ID>{code}</cbc:ID>
        <cbc:Percent>{pct}</cbc:Percent>
        <cac:TaxScheme>
          <cbc:ID>VAT</cbc:ID>
        </cac:TaxScheme>
      </cac:ClassifiedTaxCategory>
    </cac:Item>
    <cac:Price>
      <cbc:PriceAmount currencyID="{currency}">{price}</cbc:PriceAmount>
    </cac:Price>
  </cac:InvoiceLine>
""")
    subtotals = []
    total_tax = Decimal(0)
    for cat, net in nets.items():
        code, pct, rc, rs = cats[cat]
        tax = q(net * Decimal(pct) / 100, digits)
        total_tax += tax
        subtotals.append(f"""    <cac:TaxSubtotal>
      <cbc:TaxableAmount currencyID="{currency}">{fmt(net, digits)}</cbc:TaxableAmount>
      <cbc:TaxAmount currencyID="{currency}">{fmt(tax, digits)}</cbc:TaxAmount>
      {tax_category(code, pct, rc, rs).replace(chr(10) + "        ", chr(10) + "      ")}
    </cac:TaxSubtotal>
""")
    gross = total_net + total_tax
    note_xml = "".join(f"  <cbc:Note>{escape(n)}</cbc:Note>\n" for n in notes)
    xml = f"""<?xml version="1.0" encoding="UTF-8"?>
<Invoice xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
         xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"
         xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2">
  <cbc:CustomizationID>urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0</cbc:CustomizationID>
  <cbc:ProfileID>urn:fdc:peppol.eu:2017:poacc:billing:01:1.0</cbc:ProfileID>
  <cbc:ID>{inv_id}</cbc:ID>
  <cbc:IssueDate>{issue}</cbc:IssueDate>
  <cbc:DueDate>{due}</cbc:DueDate>
  <cbc:InvoiceTypeCode>380</cbc:InvoiceTypeCode>
{note_xml}  <cbc:DocumentCurrencyCode>{currency}</cbc:DocumentCurrencyCode>
  <cbc:BuyerReference>BUYER-REF-{inv_id}</cbc:BuyerReference>
{supplier}{customer}  <cac:PaymentMeans>
    <cbc:PaymentMeansCode name="Credit transfer">30</cbc:PaymentMeansCode>
    <cbc:PaymentID>{inv_id}</cbc:PaymentID>
    <cac:PayeeFinancialAccount>
      <cbc:ID>{payment[0]}</cbc:ID>
      <cbc:Name>{escape(payment[1])}</cbc:Name>
      <cac:FinancialInstitutionBranch>
        <cbc:ID>{payment[2]}</cbc:ID>
      </cac:FinancialInstitutionBranch>
    </cac:PayeeFinancialAccount>
  </cac:PaymentMeans>
  <cac:PaymentTerms>
    <cbc:Note>{escape(terms)}</cbc:Note>
  </cac:PaymentTerms>
  <cac:TaxTotal>
    <cbc:TaxAmount currencyID="{currency}">{fmt(total_tax, digits)}</cbc:TaxAmount>
{"".join(subtotals)}  </cac:TaxTotal>
  <cac:LegalMonetaryTotal>
    <cbc:LineExtensionAmount currencyID="{currency}">{fmt(total_net, digits)}</cbc:LineExtensionAmount>
    <cbc:TaxExclusiveAmount currencyID="{currency}">{fmt(total_net, digits)}</cbc:TaxExclusiveAmount>
    <cbc:TaxInclusiveAmount currencyID="{currency}">{fmt(gross, digits)}</cbc:TaxInclusiveAmount>
    <cbc:PayableAmount currencyID="{currency}">{fmt(gross, digits)}</cbc:PayableAmount>
  </cac:LegalMonetaryTotal>
{"".join(out)}</Invoice>
"""
    return xml


def write(name, xml):
    os.makedirs(OUT, exist_ok=True)
    with open(os.path.join(OUT, name), "w", encoding="utf-8", newline="\n") as f:
        f.write(xml)


# --- 1. A long invoice: 120 lines, two VAT rates, long texts --------------------------------------
PARTS = ["Hex bolt", "Washer", "Cable tie", "Ball bearing", "Gasket", "Copper pipe", "Valve", "Fuse",
         "Terminal block", "Hose clamp", "Drive belt", "Spring", "Bracket", "Sensor", "Relay"]
UNITS = ["C62", "C62", "MTR", "KGM", "C62", "HUR"]
lines = []
for n in range(120):
    name = f"{PARTS[n % len(PARTS)]} type {chr(65 + n % 26)}{n // 26 + 1}"
    desc = None
    if n % 17 == 3:
        desc = ("Long description to check the wrapping of the text in the table of the invoice lines: "
                "this item is delivered in a corrosion-resistant packaging and comes with a certificate of "
                "conformity, a data sheet and the installation instructions in four languages.")
    qty = Decimal(1 + (n * 7) % 12)
    price = Decimal(2 + (n * 37) % 180) + Decimal((n * 13) % 100) / 100
    lines.append((name, desc, qty, UNITS[n % len(UNITS)], q(price, 2), "S19" if n % 4 else "S7"))

write("phlox-long-invoice.xml", build(
    "PHLOX-LONG-1", "2026-03-02", "2026-04-01", "EUR", 2,
    ["This invoice has 120 lines, to check the page breaks, the repeated table header and the totals at the end.",
     "Prices in EUR. Delivery free to the address of the buyer."],
    party("AccountingSupplierParty", "Mustermann Industrial Supplies GmbH", "Mustermann Industrial Supplies GmbH",
          "Werkstrasse 12", "Linz", "4020", "AT", "ATU12345678", "FN123456a", "9915123456789"),
    party("AccountingCustomerParty", "Beispiel Maschinenbau AG", "Beispiel Maschinenbau AG",
          "Industrieweg 5", "Salzburg", "5020", "AT", "ATU87654321", "FN654321b", "9915987654321",
          ("Erika Beispiel", "+43 662 123456", "einkauf@beispiel.example")),
    ("AT611904300234573201", "Mustermann Industrial Supplies", "BKAUATWW"),
    "Payment within 30 days net.",
    lines,
    {"S19": ("S", "19", None, None), "S7": ("S", "7", None, None)}))

# --- 2. Cyrillic and Greek ------------------------------------------------------------------------
write("phlox-cyrillic-greek-invoice.xml", build(
    "ФАКТУРА-2026-0042", "2026-02-10", "2026-03-12", "EUR", 2,
    ["Доставка до склада на купувача. / Παράδοση στην αποθήκη του αγοραστή.",
     "Ενδοκοινοτική παράδοση – освободена доставка, чл. 138 от Директива 2006/112/ЕО."],
    party("AccountingSupplierParty", "Фотон Електроникс ООД", "Фотон Електроникс ООД",
          "ул. Околовръстен път 118", "София", "1000", "BG", "BG123456789", "123456789", "9930123456789",
          ("Иван Петров", "+359 2 123 4567", "ivan.petrov@foton.example")),
    party("AccountingCustomerParty", "Ελληνική Βιομηχανία Αισθητήρων Α.Ε.", "Ελληνική Βιομηχανία Αισθητήρων Α.Ε.",
          "Οδός Σταδίου 25", "Αθήνα", "10564", "GR", "EL123456789", "123456789", "9930987654321",
          ("Μαρία Παπαδοπούλου", "+30 210 1234567", "m.papadopoulou@sensors.example")),
    ("BG80BNBG96611020345678", "Фотон Електроникс ООД", "BNBGBGSD"),
    "Плащане в срок от 30 дни. / Πληρωμή εντός 30 ημερών.",
    [("Лазерен дистанционен сензор", "Обхват до 40 м, точност ±1 мм", Decimal(12), "C62", Decimal("84.50"), "K"),
     ("Αισθητήρας θερμοκρασίας", "Από −40 °C έως +125 °C, έξοδος 4–20 mA", Decimal(30), "C62", Decimal("12.90"), "K"),
     ("Захранващ кабел 3×1,5 мм²", None, Decimal(250), "MTR", Decimal("1.85"), "K"),
     ("Καλώδιο σήματος θωρακισμένο", "Θωράκιση πλεγμένη, εξωτερική διάμετρος 6 mm", Decimal(180), "MTR", Decimal("2.40"), "K"),
     ("Конзола за монтаж", "Неръждаема стомана", Decimal(12), "C62", Decimal("6.75"), "K"),
     ("Υπηρεσία βαθμονόμησης", "Πιστοποιητικό βαθμονόμησης ανά αισθητήρα", Decimal(42), "C62", Decimal("15.00"), "K"),
     ("Транспорт и застраховка", None, Decimal(1), "C62", Decimal("96.00"), "K")],
    {"K": ("K", "0", "VATEX-EU-IC", "Intra-community supply / Вътреобщностна доставка / Ενδοκοινοτική παράδοση")}))

# --- 3. A currency without decimals ---------------------------------------------------------------
write("phlox-jpy-invoice.xml", build(
    "SAKURA-2026-0007", "2026-04-20", "2026-05-20", "JPY", 0,
    ["All amounts in Japanese yen, which has no decimals."],
    party("AccountingSupplierParty", "Sakura Trading Co., Ltd.", "Sakura Trading Co., Ltd.",
          "1-2-3 Marunouchi, Chiyoda-ku", "Tokyo", "100-0005", "JP", "T1234567890123", "0100-01-123456", "9999123456789",
          ("Hiro Tanaka", "+81 3 1234 5678", "h.tanaka@sakura.example")),
    party("AccountingCustomerParty", "Nordic Import AB", "Nordic Import AB",
          "Drottninggatan 10", "Stockholm", "111 51", "SE", "SE556677889901", "556677-8899", "9955123456789"),
    ("JP0000000000000000", "Sakura Trading Co., Ltd.", "SAKUJPJT"),
    "Payment within 30 days.",
    [("Green tea, premium grade", "Sencha, 1 kg bag", Decimal(40), "C62", Decimal(3800), "S10"),
     ("Ceramic tea set", "Teapot and four cups", Decimal(15), "C62", Decimal(12800), "S10"),
     ("Cast iron kettle", None, Decimal(6), "C62", Decimal(24500), "S10"),
     ("Rice crackers", "Assorted, 500 g box", Decimal(120), "C62", Decimal(980), "S8"),
     ("Export packaging", None, Decimal(1), "C62", Decimal(185000), "S10"),
     ("Sea freight, Yokohama to Gothenburg", None, Decimal(1), "C62", Decimal(1250000), "S10")],
    {"S10": ("S", "10", None, None), "S8": ("S", "8", None, None)}))
print("done")
