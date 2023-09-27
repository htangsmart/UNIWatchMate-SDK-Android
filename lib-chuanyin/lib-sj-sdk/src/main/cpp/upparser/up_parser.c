#include <stdio.h>
#include <stdlib.h>
#include <memory.h>
#include <string.h>
#include <stdint.h>

#include "up_parser.h"

#define _ANDROID_ 0

#if _ANDROID_
#include <jni.h>
#include <android/log.h>

#include "up_parser.h"

#define TAG    "UPARSER_JNI" // 这个是自定义的LOG的标识
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,TAG,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,TAG,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__) // 定义LOGE类型
#else
#define LOGD(...)
#define LOGI(...)
#define LOGW(...)
#define LOGE(...)
#endif

#define  POLY_CRC16    0x1021
static uint16_t CRC16_OneByte(uint16_t lastcrc, uint16_t ddata)
{
    int i;
    for (i = 0; i < 8; i++) {
        if (((ddata << i) ^ (lastcrc >> 8)) & 0x80) {
            lastcrc <<= 1;
            lastcrc = lastcrc ^ POLY_CRC16;
        }
        else {
            lastcrc <<= 1;
        }
    }

    return lastcrc;
}

static uint16_t CalcCRC(uint16_t init, uint8_t* data, int length)
{
    int i;
    for (i = 0; i < length; i++) {
        init = CRC16_OneByte(init, data[i]);
    }
    return init;
}

static uint32_t get_4KB_len(uint32_t len)
{
    uint32_t mode = 4096 - (len % 4096);
    if (mode == 4096) mode = 0;
    return len + mode;
}

static uint8_t* malloc_and_read_up_file(const char *path, uint32_t *up_length)
{
    uint8_t* ret = 0;
    *up_length = 0;

    FILE *f = fopen(path, "rb"); if (NULL == f) return NULL;

    fseek(f, 0, SEEK_END);
    uint32_t f_size = ftell(f); if (f_size < UP_HEADER_LENGTH) return NULL;
    ret = (uint8_t*)malloc(f_size);
    if (ret) {
        fseek(f, 0, SEEK_SET);
        if (1 != fread(ret, f_size, 1, f)) {
            free(ret);
            fclose(f);
            return NULL;
        }
        *up_length = f_size;
    }
    fclose(f);
    return ret;
}

UP_CONTEXT* up_init_bytes(uint8_t* up_bytes, uint32_t f_size)
{
    UP_CONTEXT* p = 0;

    p = (UP_CONTEXT*)calloc(1, sizeof(UP_CONTEXT)); if (!p) return 0;
    memcpy(&p->up_header, up_bytes, 256);

    p->flash_length = f_size - UP_HEADER_LENGTH;

    p->up_bytes = malloc(f_size); if (!p->up_bytes) goto _ERR_RETURN;
    memcpy(p->up_bytes, up_bytes, f_size);

    p->flash_bytes = malloc(p->flash_length); if (!p->flash_bytes) goto _ERR_RETURN;
    memcpy(p->flash_bytes, up_bytes + UP_HEADER_LENGTH, p->flash_length);
    p->flash_keeped_offset = p->flash_length;

    memcpy(&p->bootloader_header, p->flash_bytes, SYS_FLASH_LAYOUT_BOOTLOADER_HEADER_LENGTH);
    if ((p->bootloader_header.boot_parition_length_with_4KB == 0) || (p->bootloader_header.boot_parition_length_with_4KB % 0x1000 != 0)) {
        p->bootloader_header.boot_parition_length_with_4KB = 0x3000;
    }
    memcpy(&p->partition_table, p->flash_bytes + (p->bootloader_header.boot_parition_length_with_4KB), SYS_FLASH_LAYOUT_PARTITION_TABLE_LENGTH);
    memcpy(&p->firmware_header, p->flash_bytes + (p->partition_table.item_firmware.addr), SYS_FLASH_LAYOUT_FIRMWARE_HEADER_LENGTH);
    return p;
_ERR_RETURN:
    up_deinit(p);
    return 0;
}

UP_CONTEXT* up_init(const char *path)
{
    uint32_t up_len = 0;
    uint8_t *up_bytes = malloc_and_read_up_file(path, &up_len);
    UP_CONTEXT* p = up_init_bytes(up_bytes, up_len);
    free(up_bytes);
    return p;
}

void up_deinit(UP_CONTEXT *p)
{
    if (p->f) fclose(p->f);
    if (p->flash_bytes) free(p->flash_bytes);
    if (p->up_bytes) free(p->up_bytes);
    if (p) free(p);
}

uint32_t up_partition_get(UP_CONTEXT*p, const char* tag,
    uint32_t *partition_length, uint32_t *others_index, uint8_t is_hole_partition)
{
    if (strcmp((const char*)p->partition_table.item_firmware.tag, (const char*)tag) == 0) {
        *partition_length = p->partition_table.item_firmware.len;
        if (is_hole_partition) {
            *partition_length = p->partition_table.others[0].addr - p->partition_table.item_firmware.addr;
        }
        return p->partition_table.item_firmware.addr;
    }
    for (uint32_t i = 0; i < p->partition_table.partition_table_num-1; i++) {
        if (strcmp((const char*)p->partition_table.others[i].tag, (const char*)tag) == 0) {
            *partition_length = p->partition_table.others[i].len;
            if (is_hole_partition) {
                *partition_length = p->partition_table.others[i + 1].addr - p->partition_table.others[i].addr;
            }
            *others_index = i;
            return p->partition_table.others[i].addr;
        }
    }
    *partition_length = 0;
    return 0;
}

int32_t up_partition_can_modify(const char *tag)
{
#define NOMODI_NUM 4
    const uint8_t* no_modifys[NOMODI_NUM] = { "HLKJ", "FIRM", "OTAP", "PSMP" };
    for (uint32_t i = 0; i < NOMODI_NUM; i++) {
        if (strcmp((const char*)tag, (const char*)no_modifys[i]) == 0)
            return 0;
    }
    return 1;
}

int32_t up_partition_table_set(UP_CONTEXT* p, uint32_t index, uint32_t addr, uint32_t length)
{
    p->partition_table.others[index].addr = addr;
    p->partition_table.others[index].len = length;
    p->partition_table.others[index].crc = CalcCRC(0xFFFF, p->flash_bytes + p->partition_table.others[index].addr,
        p->partition_table.others[index].len);
    memcpy(p->flash_bytes + (p->bootloader_header.boot_parition_length_with_4KB), (const void*)&p->partition_table,
        SYS_FLASH_LAYOUT_PARTITION_TABLE_LENGTH);
    return 0;
}

int32_t up_partition_set(UP_CONTEXT* p, const char* tag,
    uint8_t* partition_bytes, uint32_t partition_length)
{
    int32_t ret = -1;

    if (!up_partition_can_modify(tag)) return -1;

    uint32_t old_len = 0;
    uint32_t old_addr = 0;
    uint32_t old_index = 0xFF;
    old_addr = up_partition_get(p, tag, &old_len, &old_index, 1);
    uint32_t new_len = get_4KB_len(partition_length);
    if (new_len <= old_len) {
        // 塞进来的分区比原来的小或一样
        memset(p->flash_bytes + old_addr, 0, old_len);
        memcpy(p->flash_bytes + old_addr, partition_bytes, partition_length);
        up_partition_table_set(p, old_index, old_addr, partition_length);
        return 0;
    }

    // 处理塞进来的数据大于原分区长度的情况

    // 将移动的尾部数据暂存。
    uint32_t tail_len = p->flash_length - (old_addr + old_len);
    uint8_t* tail = malloc(tail_len); if (0 == tail) return -1;
    memcpy(tail, p->flash_bytes + old_addr + old_len, tail_len);

    // 扩展原flash字节数组的空间
    uint32_t incr_len = new_len - old_len;
    p->flash_bytes = (uint8_t *)realloc(p->flash_bytes, p->flash_length + incr_len); if (0 == p->flash_bytes) return -1;
    p->flash_length += incr_len;

    // 把新分区数据搞进来
    memset(p->flash_bytes + old_addr, 0, new_len);
    memcpy(p->flash_bytes + old_addr, partition_bytes, partition_length);

    // 把尾部数据搞进来
    memcpy(p->flash_bytes + old_addr + new_len, tail, tail_len);

    // 当前修改的分区表维护
    up_partition_table_set(p, old_index, old_addr, new_len);

    // 剩余所有移动了的分区表维护
    for (uint32_t i = old_index+1; i < p->partition_table.partition_table_num - 1; i++) {
        if (!up_partition_can_modify(tag))
            break;
        up_partition_table_set(p, i, p->partition_table.others[i].addr + incr_len, p->partition_table.others[i].len);
    }

    free(tail);
    return 0;
}


int32_t up_partition_keep(UP_CONTEXT* p, const char* tag, uint8_t* old_partition_table_256bytes)
{
    partition_table_t* old_table = (partition_table_t*)old_partition_table_256bytes;
    LOGE("OTAP00");
    for (uint32_t i = 0; i < old_table->partition_table_num - 1; i++) {
        if (strcmp(old_table->others[i].tag, tag) == 0) {
            LOGE("OTAP:%d", i);
            p->flash_keeped_offset = old_table->others[i].addr;
            p->partition_table.others[i] = old_table->others[i];
            LOGE("OTAP2 addr:0x%x, len:0x%x", p->partition_table.others[i].addr, p->partition_table.others[i].len);
            // up_partition_table_set(p, i, p->partition_table.others[i].addr, p->partition_table.others[i].len);
            memcpy(p->flash_bytes + (p->bootloader_header.boot_parition_length_with_4KB), (const void*)&p->partition_table,
                   SYS_FLASH_LAYOUT_PARTITION_TABLE_LENGTH);
            LOGE("OTAP3");
        }
    }
    return 0;
}


int32_t up_save(UP_CONTEXT* p, const char* path)
{
    FILE* new_up = fopen(path, "wb"); if (NULL == new_up) return -1;
    p->up_header.flash_crc = CalcCRC(0xFFFF, p->flash_bytes, p->flash_length);
    fwrite(&p->up_header, 1, sizeof(up_header_t), new_up);
    fwrite(p->flash_bytes, p->flash_keeped_offset, 1, new_up);
    fclose(new_up);
    return 0;
}
