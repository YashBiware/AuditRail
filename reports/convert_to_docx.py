import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
import re

def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = OxmlElement('w:tcMar')
    for m, val in [('w:top', top), ('w:bottom', bottom), ('w:left', left), ('w:right', right)]:
        node = OxmlElement(m)
        node.set(qn('w:w'), str(val))
        node.set(qn('w:type'), 'dxa')
        tcMar.append(node)
    tcPr.append(tcMar)

def create_report_docx(md_path, docx_path):
    doc = docx.Document()
    
    # Page setup - Standard A4 and Margins (Left 30mm=1.18in, Right 20mm=0.79in, Top 25mm=0.98in, Bottom 22mm=0.87in)
    section = doc.sections[0]
    section.page_width = Inches(8.27)
    section.page_height = Inches(11.69)
    section.left_margin = Inches(1.18)
    section.right_margin = Inches(0.79)
    section.top_margin = Inches(1.0)
    section.bottom_margin = Inches(0.87)
    
    # Base Normal style
    style = doc.styles['Normal']
    font = style.font
    font.name = 'Times New Roman'
    font.size = Pt(12)
    font.color.rgb = RGBColor(0, 0, 0)
    
    with open(md_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
        
    in_table = False
    table_rows = []
    
    def flush_table():
        nonlocal in_table, table_rows
        if not table_rows:
            in_table = False
            return
        
        # Clean table rows
        parsed_rows = []
        for r in table_rows:
            if re.match(r'^\s*\|?\s*:?-+:?\s*(\|?\s*:?-+:?\s*)+\|?\s*$', r):
                continue
            cols = [c.strip() for c in r.split('|')]
            if cols and cols[0] == '':
                cols = cols[1:]
            if cols and cols[-1] == '':
                cols = cols[:-1]
            if any(cols):
                parsed_rows.append(cols)
                
        if parsed_rows:
            col_count = max(len(r) for r in parsed_rows)
            tbl = doc.add_table(rows=len(parsed_rows), cols=col_count)
            tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
            
            for r_idx, row_data in enumerate(parsed_rows):
                for c_idx in range(col_count):
                    val = row_data[c_idx] if c_idx < len(row_data) else ""
                    cell = tbl.cell(r_idx, c_idx)
                    cell.text = val
                    set_cell_margins(cell, 120, 120, 150, 150)
                    for p in cell.paragraphs:
                        p.paragraph_format.line_spacing = 1.15
                        p.paragraph_format.space_after = Pt(2)
                        for run in p.runs:
                            run.font.name = 'Times New Roman'
                            run.font.size = Pt(10)
                            if r_idx == 0:
                                run.font.bold = True
                                
        doc.add_paragraph() # Spacing
        table_rows = []
        in_table = False

    for line in lines:
        raw = line.rstrip()
        
        # Check table
        if raw.strip().startswith('|'):
            in_table = True
            table_rows.append(raw)
            continue
        elif in_table:
            flush_table()
            
        if raw == '---':
            doc.add_page_break()
            continue
            
        if not raw.strip():
            continue
            
        if raw.startswith('# '):
            text = raw[2:].strip()
            p = doc.add_paragraph()
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            p.paragraph_format.space_before = Pt(18)
            p.paragraph_format.space_after = Pt(12)
            run = p.add_run(text)
            run.font.name = 'Times New Roman'
            run.font.size = Pt(16)
            run.font.bold = True
        elif raw.startswith('## '):
            text = raw[3:].strip()
            p = doc.add_paragraph()
            p.alignment = WD_ALIGN_PARAGRAPH.LEFT
            p.paragraph_format.space_before = Pt(14)
            p.paragraph_format.space_after = Pt(6)
            run = p.add_run(text)
            run.font.name = 'Times New Roman'
            run.font.size = Pt(14)
            run.font.bold = True
        elif raw.startswith('### '):
            text = raw[4:].strip()
            p = doc.add_paragraph()
            p.alignment = WD_ALIGN_PARAGRAPH.LEFT
            p.paragraph_format.space_before = Pt(10)
            p.paragraph_format.space_after = Pt(4)
            run = p.add_run(text)
            run.font.name = 'Times New Roman'
            run.font.size = Pt(12)
            run.font.bold = True
        elif raw.startswith('- ') or raw.startswith('* '):
            text = raw[2:].strip()
            p = doc.add_paragraph(style='List Bullet')
            p.paragraph_format.line_spacing = 1.3
            p.paragraph_format.space_after = Pt(3)
            # handle simple bold
            parts = re.split(r'(\*\*.*?\*\*)', text)
            for part in parts:
                if part.startswith('**') and part.endswith('**'):
                    r = p.add_run(part[2:-2])
                    r.font.name = 'Times New Roman'
                    r.font.size = Pt(12)
                    r.font.bold = True
                else:
                    r = p.add_run(part)
                    r.font.name = 'Times New Roman'
                    r.font.size = Pt(12)
        else:
            p = doc.add_paragraph()
            p.paragraph_format.line_spacing = 1.5
            p.paragraph_format.space_after = Pt(6)
            parts = re.split(r'(\*\*.*?\*\*)', raw)
            for part in parts:
                if part.startswith('**') and part.endswith('**'):
                    r = p.add_run(part[2:-2])
                    r.font.name = 'Times New Roman'
                    r.font.size = Pt(12)
                    r.font.bold = True
                else:
                    r = p.add_run(part)
                    r.font.name = 'Times New Roman'
                    r.font.size = Pt(12)
                    
    if in_table:
        flush_table()
        
    doc.save(docx_path)
    print("DOCX successfully generated:", docx_path)

if __name__ == '__main__':
    create_report_docx(
        r'C:\Users\yashb\Desktop\AuditRail\reports\AuditRail_Project_Report.md',
        r'C:\Users\yashb\Desktop\AuditRail\reports\AuditRail_Project_Report.docx'
    )
