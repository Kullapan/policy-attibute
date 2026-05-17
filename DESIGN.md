# Policy Attribute Manager — Design System Documentation

> **Source:** Stitch Project `12761328813621493061` — *Policy Attribute Manager*  
> **Device Target:** Desktop (1280px+)  
> **Theme Mode:** Light  
> **Generated:** 2026-04-18  

---

## Table of Contents

1. [Creative North Star](#1-creative-north-star)
2. [Color System](#2-color-system)
3. [Typography](#3-typography)
4. [Spacing & Shape](#4-spacing--shape)
5. [Elevation & Depth](#5-elevation--depth)
6. [Component Specifications](#6-component-specifications)
7. [Screen Inventory](#7-screen-inventory)
8. [Do's and Don'ts](#8-dos-and-donts)

---

## 1. Creative North Star

**Concept: "The Institutional Architect"**

In the world of high-stakes finance and insurance, trust is not built through decorative flair—it is built through precision, clarity, and an authoritative editorial voice.

This design system rejects the "generic SaaS" look in favor of a bespoke, high-end editorial experience. It employs intentional asymmetry: pairing high-density data modules with expansive breathing white space. By layering surfaces and using a sophisticated typography scale, the UI feels like a **premium financial broadsheet**—efficient for the power user, prestigious for the stakeholder.

---

## 2. Color System

### 2.1 Brand Override Palette

These are the top-level brand colors that override Material You generation:

| Role        | Hex       | Usage                                               |
|-------------|-----------|-----------------------------------------------------|
| **Primary** | `#00008F` | Core brand navy — all primary actions and headings  |
| **Secondary** | `#F26522` | Energetic orange — surgical accent for critical CTAs |
| **Tertiary** | `#009CDE` | Sky blue — informational highlights and links       |
| **Neutral** | `#2E3644` | Dark slate — text shadow tints, neutral tokens      |

### 2.2 Full Named Color Tokens

#### Surface Hierarchy (Light Mode)

| Token                        | Hex       | Role                                           |
|------------------------------|-----------|------------------------------------------------|
| `surface` / `background`     | `#f9f9ff` | Global page background — the foundation layer  |
| `surface_bright`             | `#f9f9ff` | Same as surface; used for elevated bright zones|
| `surface_container_lowest`   | `#ffffff`  | Cards, input fields — the topmost layer        |
| `surface_container_low`      | `#f0f3ff` | Sidebars, secondary regions                    |
| `surface_container`          | `#e7eeff` | Info panels, tabs                              |
| `surface_container_high`     | `#e0e8fb` | Active surfaces, hovered rows                  |
| `surface_container_highest`  | `#dbe3f5` | Emphasized data clusters, active interactive zones |
| `surface_dim`                | `#d2daec` | Footer areas, de-emphasized regions            |
| `surface_variant`            | `#dbe3f5` | Alternate surface for visual differentiation   |

#### Primary Palette

| Token                    | Hex       |
|--------------------------|-----------|
| `primary`                | `#000051` |
| `primary_container`      | `#00008f` |
| `primary_fixed`          | `#e0e0ff` |
| `primary_fixed_dim`      | `#bfc2ff` |
| `on_primary`             | `#ffffff` |
| `on_primary_container`   | `#7a82f9` |
| `on_primary_fixed`       | `#00006e` |
| `on_primary_fixed_variant` | `#2f36ac` |
| `inverse_primary`        | `#bfc2ff` |

#### Secondary Palette (Energetic Orange)

| Token                      | Hex       |
|----------------------------|-----------|
| `secondary`                | `#a63b00` |
| `secondary_container`      | `#fc6c29` |
| `secondary_fixed`          | `#ffdbce` |
| `secondary_fixed_dim`      | `#ffb599` |
| `on_secondary`             | `#ffffff` |
| `on_secondary_container`   | `#5a1c00` |
| `on_secondary_fixed`       | `#370e00` |
| `on_secondary_fixed_variant` | `#7f2b00` |

#### Tertiary Palette (Sky Blue)

| Token                     | Hex       |
|---------------------------|-----------|
| `tertiary`                | `#001421` |
| `tertiary_container`      | `#002a40` |
| `tertiary_fixed`          | `#c9e6ff` |
| `tertiary_fixed_dim`      | `#89ceff` |
| `on_tertiary`             | `#ffffff` |
| `on_tertiary_container`   | `#0097d7` |
| `on_tertiary_fixed`       | `#001e2f` |
| `on_tertiary_fixed_variant` | `#004c6e` |

#### Neutral / On-Surface Tokens

| Token                | Hex       | Usage                                          |
|----------------------|-----------|------------------------------------------------|
| `on_surface`         | `#141c29` | Primary text — all body copy and headings      |
| `on_surface_variant` | `#454653` | Secondary text — metadata, labels              |
| `on_background`      | `#141c29` | Text on main background                        |
| `outline`            | `#767685` | Subtle outlines, captions                      |
| `outline_variant`    | `#c6c5d5` | Ghost border base — used at 20% opacity        |
| `inverse_surface`    | `#29313e` | Dark overlay backgrounds                       |
| `inverse_on_surface` | `#ebf1ff` | Text on dark overlays                          |
| `surface_tint`       | `#4950c5` | Tonal tint for elevation effects               |

#### Error Tokens

| Token              | Hex       |
|--------------------|-----------|
| `error`            | `#ba1a1a` |
| `error_container`  | `#ffdad6` |
| `on_error`         | `#ffffff` |
| `on_error_container` | `#93000a` |

### 2.3 Key Design Rules

#### The "No-Line" Rule
> **1px solid borders are prohibited for sectioning content.**

Boundaries must be defined solely through background color shifts or tonal transitions.

```
✅ sidebar: surface_container_low (#f0f3ff)
   main:    surface (#f9f9ff)
   — The 3% luminosity gap creates the boundary.

❌ border: 1px solid #c6c5d5;  /* NEVER for layout sectioning */
```

#### The "Glass & Gradient" Rule
Floating elements (Modals, Dropdowns, Sticky Navs) must use **Glassmorphism**:

```css
background: rgba(249, 249, 255, 0.75);
backdrop-filter: blur(20px);
-webkit-backdrop-filter: blur(20px);
```

#### Primary Gradient (Signature Texture)
Main CTAs and Hero sections use a gradient instead of flat color:

```css
background: linear-gradient(135deg, #000051, #00008f);
```

---

## 3. Typography

### 3.1 Font Strategy — Dual Font System

| Font Family   | Role                               | Source            |
|---------------|------------------------------------|-------------------|
| **Public Sans** | Headlines, Display, Titles       | Google Fonts      |
| **Inter**     | Body copy, Labels, Data cells      | Google Fonts      |

### 3.2 Type Scale

| Level         | Font        | Size       | Weight   | Color      | Usage                                |
|---------------|-------------|------------|----------|------------|--------------------------------------|
| Display LG    | Public Sans | 2.5rem     | Bold     | `#000051`  | KPI numbers, hero stats              |
| Headline LG   | Public Sans | 2rem       | SemiBold | `#141c29`  | Dashboard/page titles                |
| Headline MD   | Public Sans | 1.5rem     | SemiBold | `#141c29`  | Section headers                      |
| Title MD      | Public Sans | 1.125rem   | Medium   | `#141c29`  | Card titles, panel headers           |
| Body MD       | Inter       | 0.875rem   | Regular  | `#454653`  | Data cells, form values, paragraphs  |
| Body SM       | Inter       | 0.8125rem  | Regular  | `#454653`  | Secondary data, metadata             |
| Label MD      | Inter       | 0.75rem    | Bold     | `#767685`  | Table column headers (ALL CAPS, 0.05em tracking) |
| Label SM      | Inter       | 0.6875rem  | Medium   | `#767685`  | Metadata, timestamps, captions       |

### 3.3 Typography Rules

- **Never use pure `#000000`** for text. Always use `on_surface` (`#141c29`).
- Table column headers: ALL CAPS + `letter-spacing: 0.05em` for editorial authority.
- Inter is the exclusive font for any text inside data tables or forms.
- Public Sans should feel "architectural and set in stone" — use only for structural labels.

---

## 4. Spacing & Shape

### 4.1 Border Radius Scale

| Token  | Value     | Usage                                              |
|--------|-----------|----------------------------------------------------|
| `sm`   | `0.25rem` | Tags, small chips                                  |
| `md`   | `0.375rem`| Standard components — inputs, buttons, cards (**default**) |
| `lg`   | `0.5rem`  | Modals, large panels                               |
| `xl`   | `0.75rem` | Special decorative containers                      |
| `full` | `9999px`  | Pills, status chips, avatar indicators             |

> ⚠️ `roundness` config: `ROUND_FOUR` — equates to `0.375rem` (`md`) as the standard.  
> Sharp corners (`0`) are **prohibited** anywhere in the UI.

### 4.2 Spacing Scale (`spacingScale: 1`)

| Name  | Value  | Usage                                      |
|-------|--------|--------------------------------------------|
| `2xs` | 4px    | Icon internal padding, tight gaps          |
| `xs`  | 8px    | Between related inline items               |
| `sm`  | 12px   | Table cell vertical padding                |
| `md`  | 16px   | Standard component padding                 |
| `lg`  | 24px   | Table cell horizontal padding, card insets |
| `xl`  | 32px   | Section gap, card-to-card spacing          |
| `2xl` | 48px   | Panel margins                              |
| `3xl` | 64px   | Page margin minimum (required around tables) |

---

## 5. Elevation & Depth

This system achieves depth through **Tonal Layering**, not traditional drop shadows.

### 5.1 The Layering Principle

```
Layer 0 — Page Background:       surface           (#f9f9ff)
Layer 1 — Secondary Regions:     surface_container_low  (#f0f3ff)
Layer 2 — Cards / Work Areas:    surface_container_lowest (#ffffff)
Layer 3 — Emphasized Clusters:   surface_container_highest (#dbe3f5)
Layer 4 — Floating / Glass:      rgba(#f9f9ff, 0.75) + blur(20px)
```

### 5.2 Shadow Tokens

| Context               | CSS Value                                              |
|-----------------------|--------------------------------------------------------|
| **Ambient (Modals)**  | `box-shadow: 0 12px 40px rgba(20, 28, 41, 0.06)`       |
| **Tooltip / Dropdown**| `box-shadow: 0 4px 16px rgba(20, 28, 41, 0.08)`        |
| **Ghost Border**      | `outline: 1px solid rgba(198, 197, 213, 0.20)`         |

> Use `on_surface` (`#141c29`) as the shadow **tint**, never pure `#000000`.

---

## 6. Component Specifications

### 6.1 Buttons

#### Primary Button
```css
background: linear-gradient(135deg, #000051, #00008f);
color: #ffffff;                   /* on_primary */
border-radius: 0.375rem;          /* md */
padding: 10px 24px;
font: 600 0.875rem/1 "Inter";
border: none;
```
**Hover:** Lighten gradient by ~8%, add `box-shadow: 0 4px 12px rgba(20,28,41,0.12)`.

#### Secondary Button (Precision Action)
```css
background: #a63b00;              /* secondary */
color: #ffffff;
border-radius: 0.375rem;
```
> Use **sparingly** — only for irreversible or high-stakes actions (e.g., "Submit Claim", "Execute Upload").

#### Tertiary Button (Text Action)
```css
background: transparent;
border: none;
color: #000051;                   /* primary */
text-decoration: underline 2px transparent;
transition: text-decoration-color 0.2s;
```
**Hover:** `text-decoration-color: #000051;`

### 6.2 Form Inputs

```css
background: #ffffff;              /* surface_container_lowest */
border: 1px solid rgba(198, 197, 213, 0.20);   /* ghost border */
border-radius: 0.375rem;
padding: 10px 16px;
font: 400 0.875rem "Inter";
color: #141c29;
transition: border-color 0.2s, box-shadow 0.2s;
```

**Focus State:**
```css
border: 2px solid #002a40;        /* tertiary_container */
box-shadow: 0 0 0 3px rgba(0, 42, 64, 0.08);
outline: none;
```

**Label Layout (Asymmetric Two-Column Grid):**
```css
.form-row {
  display: grid;
  grid-template-columns: 33% 67%;
  align-items: center;
  gap: 8px 16px;
}
/* Labels left (33%), Inputs right (67%) */
```

### 6.3 Data Tables

```css
/* Container */
.data-table {
  border-collapse: collapse;
  width: 100%;
}

/* Header Row */
.data-table thead th {
  font: 700 0.75rem/1 "Inter";
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #000051;               /* primary */
  padding: 12px 24px;
  background: #dbe3f5;          /* surface_container_highest */
  position: sticky;
  top: 0;
}

/* Data Rows — No dividers, alternating backgrounds */
.data-table tbody tr:nth-child(odd)  { background: #f9f9ff; }  /* surface */
.data-table tbody tr:nth-child(even) { background: #f0f3ff; }  /* surface_container_low */

/* Cell Padding */
.data-table tbody td {
  padding: 12px 24px;           /* tight vertical, generous horizontal */
  font: 400 0.875rem "Inter";
  color: #141c29;
}

/* Row Hover */
.data-table tbody tr:hover {
  background: #e0e8fb;          /* surface_container_high */
}
```

> **Rule:** No vertical lines, no horizontal dividers between rows. Use background alternation only.

### 6.4 Status Chips

```css
.chip {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 9999px;        /* full */
  font: 600 0.6875rem "Inter";
  letter-spacing: 0.03em;
}

/* Neutral state */
.chip--neutral {
  background: #e0e0ff;          /* primary_fixed */
  color: #00006e;               /* on_primary_fixed */
}

/* Alert / Warning state */
.chip--alert {
  background: #ffdbce;          /* secondary_fixed */
  color: #370e00;               /* on_secondary_fixed */
}

/* Error state */
.chip--error {
  background: #ffdad6;          /* error_container */
  color: #93000a;               /* on_error_container */
}
```

> **Rule:** Avoid high-saturation "traffic light" colors (pure red/green/amber). Use muted, professional tones only.

### 6.5 Modals / Dialogs

```css
.modal-overlay {
  background: rgba(41, 49, 62, 0.4);  /* inverse_surface at 40% */
  backdrop-filter: blur(4px);
}

.modal {
  background: rgba(249, 249, 255, 0.92);  /* surface glassmorphism */
  backdrop-filter: blur(20px);
  border-radius: 0.5rem;                  /* lg */
  box-shadow: 0 12px 40px rgba(20, 28, 41, 0.06);
  padding: 32px;
}
```

### 6.6 Navigation / Sidebar

```css
.sidebar {
  background: #f0f3ff;          /* surface_container_low */
  width: 240px;
  min-height: 100vh;
  padding: 24px 0;
}

.nav-item {
  padding: 10px 24px;
  font: 500 0.875rem "Inter";
  color: #454653;               /* on_surface_variant */
  border-radius: 0 0.375rem 0.375rem 0;
  transition: background 0.15s;
}

.nav-item--active {
  background: #e7eeff;          /* surface_container */
  color: #000051;               /* primary */
  font-weight: 600;
}

.nav-item:hover {
  background: #e0e8fb;          /* surface_container_high */
}
```

### 6.7 Page Layout Structure

```
┌─────────────────────────────────────────────────────┐
│  Top Nav (sticky, glassmorphism)          64px tall  │
├──────────────┬──────────────────────────────────────┤
│              │                                       │
│  Sidebar     │  Main Content Area                    │
│  240px       │  Flexible — min 64px page margins     │
│  surface_    │  surface (#f9f9ff)                    │
│  container_  │                                       │
│  low         │  ┌───────────────────────────────┐   │
│              │  │  Card (surface_container_      │   │
│              │  │  lowest / #ffffff)             │   │
│              │  └───────────────────────────────┘   │
│              │                                       │
└──────────────┴──────────────────────────────────────┘
│  Footer  (surface_dim / #d2daec)                     │
└─────────────────────────────────────────────────────┘
```

---

## 7. Screen Inventory

The project contains **6 screens** across the Policy Attribute Manager application:

| # | Screen Title                  | Screen ID                              | Dimensions | Notes                             |
|---|-------------------------------|----------------------------------------|------------|-----------------------------------|
| 1 | Policy Attribute Management PRD | `3fe6311803084716a2f34b71c0dae20d`   | 600×600    | Requirements doc view (Markdown)  |
| 2 | Attribute Dictionary          | `da2c102150cd40fc88c1aaf14e19b99e`   | 1280×1024  | Master list of all attributes     |
| 3 | Attribute Configuration       | `ee0cc8431773464e8a4a82c0d5c2f007`   | 1280×1024  | Detail/edit view for one attribute|
| 4 | Select Attribute Modal        | `d4eaa1688e5f4dbcbeabc03e250ebd7d`   | 1280×1024  | Dialog to browse & pick attributes|
| 5 | Policy Attribute Mapping      | `f3e635e5a8e84c10bffedee1e7a708c1`   | 1280×1088  | Maps attributes to policy types   |
| 6 | Bulk Upload Manager           | `ef559342976245acb077dfadc3b44bce`   | 1280×1233  | CSV/Excel bulk attribute ingestion|

### Screen Descriptions

#### Screen 1 — Policy Attribute Management PRD
A structured requirements document rendered in-app. Acts as the source of truth for product requirements. Contains markdown-formatted specs.

#### Screen 2 — Attribute Dictionary
The core browsing interface for the entire attribute library. Features:
- High-density data table (alternating rows, sticky header)
- Left sidebar for category/type filtering
- Search bar with instant filter
- Column: Attribute Name, Data Type, Category, Status chip, Actions

#### Screen 3 — Attribute Configuration
Detail view for viewing and editing a single attribute's full configuration:
- Asymmetric two-column form layout (33/67 grid)
- Multi-section accordion for metadata, validation rules, and usage
- Save/Cancel button pair (primary gradient + tertiary text)
- Status chip in header

#### Screen 4 — Select Attribute Modal
A modal search-and-select experience for linking attributes where:
- Glassmorphism modal overlay
- Searchable, scrollable attribute list
- Multi-select with chip visualization of selected items
- Confirm (primary) and Cancel (tertiary) buttons

#### Screen 5 — Policy Attribute Mapping
The relationship view between policy types and their assigned attributes:
- Master–detail split layout
- Left: policy type list (surface_container_low panel)
- Right: attribute assignment table for selected policy
- Drag-and-drop or "Add Attribute" CTA (secondary orange button)

#### Screen 6 — Bulk Upload Manager
Multi-step upload workflow for batch attribute import:
- Step indicator at top (progress stepper)
- File drag-and-drop zone (dashed ghost border, surface_container_lowest)
- Validation results table with error chips
- Download template (tertiary text link) + Upload (primary gradient button)

---

## 8. Do's and Don'ts

### ✅ Do

| Rule | Description |
|------|-------------|
| **Tonal boundaries** | Use background color shifts (e.g., `#f0f3ff` vs `#f9f9ff`) to separate sections — never 1px lines |
| **Whitespace generosity** | Surround data tables with at least `64px` page margins to maintain a premium feel |
| **Surgical secondaries** | Use `secondary` (`#a63b00`) for only one critical call-to-action per screen |
| **Glassmorphism for floats** | All modals, dropdowns, and sticky navbars must use `backdrop-filter: blur(20px)` |
| **Gradient CTAs** | Primary buttons always use `linear-gradient(135deg, #000051, #00008f)` — never flat |
| **Footer anchoring** | Use `surface_dim` (`#d2daec`) as the footer background to visually ground the layout |
| **Muted status colors** | Status chips must use `primary_fixed` (neutral) or `secondary_fixed` (alert) — no traffic-light hues |
| **Inter for data** | All text inside tables and forms must use Inter for legibility |
| **Public Sans for authority** | Dashboard titles and section headers use Public Sans for editorial weight |

### ❌ Don't

| Anti-pattern | Reason |
|--------------|--------|
| `color: #000000` | Too harsh. Always use `on_surface` (`#141c29`) |
| `border: 1px solid` for layout sections | Creates visual noise; use background tonal shifts instead |
| Dividers between table rows | Forbidden; use alternating row backgrounds |
| `border-radius: 0` (sharp corners) | Everything follows `md` (0.375rem) minimum roundness |
| Standard box shadows (heavy) | Use tonal layering + `rgba(20,28,41, 0.06)` ambient shadows |
| High-saturation status colors | Muted professional tones only — no pure red/green/yellow chips |
| Separating list items with 1px lines | Use 4px vertical spacing gap instead |
| `xl` or `full` roundness on structural components | Reserved for pills and small chips only |

---

## Appendix — Stitch Project Metadata

```yaml
project_id:    "12761328813621493061"
project_name:  "projects/12761328813621493061"
title:         "Policy Attribute Manager"
visibility:    PRIVATE
origin:        STITCH
project_type:  TEXT_TO_UI_PRO
device_type:   DESKTOP
color_mode:    LIGHT
color_variant: FIDELITY
font_headline: PUBLIC_SANS
font_body:     INTER
font_label:    INTER
roundness:     ROUND_FOUR   # 0.375rem (md)
spacing_scale: 1
created:       2026-04-18T04:52:40Z
updated:       2026-04-18T04:59:43Z
screen_count:  6
```
