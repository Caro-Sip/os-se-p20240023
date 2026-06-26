# 1. Store the configuration variables
P = 16  # Page size
# The page table mapping from Part 1A (valid pages only)
page_table = {0: 5, 1: 2, 2: 1, 4: 7, 6: 0, 7: 4}

# The 6 logical addresses to translate (including N = 32 and invalid page 48)
logical_addresses = [20, 100, 48, 16, 127, 32]

# 2. Run the translation loop
for LA in logical_addresses:
    page = LA // P
    offset = LA % P

    if page in page_table:
        frame = page_table[page]
        physical = (frame * P) + offset
        print(f"Logical {LA:3} -> page {page}, offset {offset:2} -> frame {frame} -> physical {physical}")
    else:
        print(f"Logical {LA:3} -> page {page} -> Page fault: page not in memory")
