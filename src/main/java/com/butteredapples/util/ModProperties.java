package com.butteredapples.util;

import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.List;

public class ModProperties {

    public static final EnumProperty<BookEnum> BOOK_TYPE_SLOT_0 = EnumProperty.create("book_type_0", BookEnum.class);
    public static final EnumProperty<BookEnum> BOOK_TYPE_SLOT_1 = EnumProperty.create("book_type_1", BookEnum.class);
    public static final EnumProperty<BookEnum> BOOK_TYPE_SLOT_2 = EnumProperty.create("book_type_2", BookEnum.class);
    public static final EnumProperty<BookEnum> BOOK_TYPE_SLOT_3 = EnumProperty.create("book_type_3", BookEnum.class);
    public static final EnumProperty<BookEnum> BOOK_TYPE_SLOT_4 = EnumProperty.create("book_type_4", BookEnum.class);
    public static final EnumProperty<BookEnum> BOOK_TYPE_SLOT_5 = EnumProperty.create("book_type_5", BookEnum.class);

    public static final List<EnumProperty<BookEnum>> BOOK_TYPE_PROPERTIES = List.of(
            ModProperties.BOOK_TYPE_SLOT_0,
            ModProperties.BOOK_TYPE_SLOT_1,
            ModProperties.BOOK_TYPE_SLOT_2,
            ModProperties.BOOK_TYPE_SLOT_3,
            ModProperties.BOOK_TYPE_SLOT_4,
            ModProperties.BOOK_TYPE_SLOT_5
    );
}
