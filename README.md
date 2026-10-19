<!--
SPDX-FileCopyrightText: 2026 Cedric Hammes
SPDX-License-Identifier: MIT
-->

# Fabric Excavator Mod

> **Software Notice:** This project was programmed with the assistance of a large language model
> (LLM). The code was reviewed by a human, whose involvement was limited to guidance and review.

Fabric Excavator lets you select a cuboid region and excavate all blocks in it by breaking a single
block. The excavation uses the mining speed of the slowest block in the selection and processes
every block through Minecraft's regular server-side breaking logic.

## Usage

1. Hold `V` and left-click the first corner block.
2. Hold `V` and left-click the opposite corner block.
3. Check the highlighted cuboid region.
4. Release `V` and break any highlighted block to excavate the selection.

## Requirements

- Minecraft 1.21.5
- Fabric Loader 0.19.5 or newer
- Fabric API 0.128.2+1.21.5
- Fabric Language Kotlin 1.13.2+kotlin.2.1.20 or newer
- Java 21

The selection key can be changed in Minecraft's key binding settings. Starting another selection
replaces the current region.

## License

This project is licensed under the MIT License. See `LICENSE` for details.
