<!--
SPDX-FileCopyrightText: 2026 Cedric Hammes
SPDX-License-Identifier: MIT
-->

# AGENTS.md

## Working Style

- When I ask you to fix or change something, work on the EXACT file I specify. Do not edit related
  files, adjacent modules, or
  'nearby' code unless explicitly asked. If you're unsure which file to edit, ask first.
- NEVER explore Gradle caches, .gradle directories, or external library internals to understand
  dependencies. If you need to understand an external API, ask me or check official docs via
  WebFetch. Do not spelunk.
- Explain your plan BEFORE making edits, especially for non-trivial changes. Do not jump straight to
  editing files. Wait for my approval of the approach.
- Do not make changes beyond what I asked for. No 'while I'm here' improvements, no proactive
  additions to other files, no scope creep. If you think something adjacent should also change,
  mention it but don't do it.
- When I explicitly tell you to skip something, leave something commented out, or not touch a
  specific thing - respect that completely. Do not revisit, re-enable, or re-suggest it.
- Avoid dense expression chains with trailing Elvis fallbacks or `?: run { ... }` blocks. Prefer
  named local values, early returns, or small helpers when fallback logic has multiple steps.
- Avoid large stacked boolean conditions in if statements. Split them into named boolean values that
  describe the actual state being checked.
- Always use braces for `if` and `else` branches, including single-statement branches and Kotlin
  `if` expressions.
- Write direct statements. Do not use "X, not Y" framing or variants such as "not X but Y", "this
  isn't X, it's Y", or "X rather than Y". Omit irrelevant counterpoints. If both facts matter, state
  them in separate sentences. Example: "The compiler thresholds remain undecided." Check for and
  remove this framing from documentation, code comments, and assistant responses before delivering
  them.
- Avoid bullet catalogs made of bold category labels followed by a colon and a one-line description,
  such as
  `- **Formatting:** ...`. When explaining use cases or ideas, give each a short section with a
  concrete example and enough prose to explain it. Use bullets for actual lists, steps, or short
  items that are easier to read together.
- Write comments and KDocs like you're explaining something to a colleague and use plain language.
  Use short plain sentences with contractions. Put one fact in each sentence. Don't chain clauses
  with commas, such as opening with "With a parallelism of one, reads run inline" or trailing ", at
  most N at a time". Don't overcorrect into terse fragments either. Keep inline comments to one
  sentence and skip them when the code already says it.
- Document nontrivial functions. Start with a short functional description. Follow it with a blank
  documentation line and a more detailed description in several sentences. Add `# Security Notice`
  only when the function has a relevant security concern. Add `# Errors` for functions that can fail
  and list actual error cases as ``- [`Error::Type`]: reason``. A.e.:
  ```
  <Short function description>
  
  <Long function description>
  
  # Security Notice
  <If required, notice about security implications of this feature>
  
  # Errors
  <The enumeration of possible errors>
  ```
- Put subsection content immediately below every documentation heading. Do not add a blank `///`
  line between a heading and its content. Omit documentation for one-line or trivially
  self-explanatory functions.
- Add short `//` comments to code sections whose purpose is not immediately clear. Explain why the
  code is needed.
