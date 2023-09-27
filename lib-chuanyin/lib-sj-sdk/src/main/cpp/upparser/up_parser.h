#include <stdint.h>

#define UP_HEADER_LENGTH                                    0x100

#define SYS_FLASH_LAYOUT_BOOTLOADER_START                   0
#define SYS_FLASH_LAYOUT_BOOTLOADER_HEADER_LENGTH           0x60
#define SYS_FLASH_LAYOUT_PARTITION_TABLE_LENGTH             0x100
#define SYS_FLASH_LAYOUT_FIRMWARE_HEADER_LENGTH             0x30

#pragma pack (1)

typedef struct up_header_s{
    uint8_t tag[6];
    uint32_t header_len;
    uint8_t encrypt_en;
    uint8_t flash_linemode;
    uint8_t flash_div;
    uint8_t spi_mode;
    uint8_t chip_sel;
    uint8_t resolved1;
    uint32_t flash_len;
    uint16_t flash_crc;
    uint8_t paltform[10];
    uint8_t reserved[222];
    uint8_t tail[2];
}up_header_t;

// the size shell be 0x60
typedef struct _boot_loader_header_s {
    char tag[4];
    uint32_t ram_addr_write;
    uint32_t ram_addr_run;
    uint32_t boot_addr;
    uint32_t boot_len;
    uint32_t boot_crc;
    uint32_t norf_baud;
    uint32_t norf_line;
    uint32_t boot_parition_length_with_4KB;
    uint32_t reserved1[2];
    uint32_t flag;
    uint32_t reserved2[10];
    uint32_t head_crc;
}boot_loader_header_t;

// 16 bytes
typedef struct {
    uint8_t tag[4];
    uint32_t addr;
    uint32_t len;
    uint32_t crc;
}partition_table_item_t;

// 256 bytes
typedef struct _partition_table_s {
    uint32_t partition_table_num;
    uint32_t version;
    uint8_t  reserved[8];
    partition_table_item_t item_firmware;
    partition_table_item_t others[14];
}partition_table_t;

// 0x30 bytes
typedef struct _firmware_header_s {
    uint32_t firmware_header_len;
    uint32_t timestamp;
    uint8_t  reserved[8];
    uint32_t loadToRam;
    uint32_t runFromRam;
    uint32_t loadLength;
    uint32_t loadCrc;
    uint8_t   reserved0[16];
}firmware_header_t;

// 对象本体
typedef struct {
    FILE* f;
    up_header_t up_header;
    uint8_t* up_bytes;
    uint8_t* flash_bytes;
    uint32_t flash_length;
    uint32_t flash_keeped_offset;
    boot_loader_header_t bootloader_header;
    partition_table_t    partition_table;
    firmware_header_t    firmware_header;
}UP_CONTEXT;

#pragma pack()



#ifdef __cplusplus
extern "C" {
#endif

UP_CONTEXT* up_init_bytes(uint8_t* up_bytes, uint32_t f_size);
UP_CONTEXT* up_init(const char *path);
void up_deinit(UP_CONTEXT *p);

uint32_t up_partition_get(UP_CONTEXT* p, const char* tag,
    uint32_t* partition_length, uint32_t* others_index, uint8_t is_hole_partition);

// 填入新分区数据，（通用接口）
int32_t up_partition_set(UP_CONTEXT* p, const char* tag, uint8_t* partition_bytes, uint32_t partition_length);

// 分区保留
int32_t up_partition_keep(UP_CONTEXT* p, const char* tag, uint8_t* old_partition_table_256bytes);

int32_t up_save(UP_CONTEXT* p, const char *path);

#ifdef __cplusplus
}
#endif

