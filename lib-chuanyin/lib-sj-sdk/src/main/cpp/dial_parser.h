//
// Created by wangwenfeng on 2023/3/29.
//

#ifndef FUN_DIAL_PARSER_H
#define FUN_DIAL_PARSER_H

#include <string>
#include <vector>
#include <cstdint>
#include <memory>

struct dialresource_info_t {
    uint8_t type;
    uint8_t fmt;
    uint16_t reserved;
    union {
        uint8_t img_para[24];
        uint8_t placeholder[24];
    } extra_para;
};

struct dial_thumbnail_t {
    uint32_t offset;
    uint32_t size;
    dialresource_info_t thumbnail_info;
};

struct dial_thumbnail_info_t {
    uint16_t num;
    std::vector<dial_thumbnail_t> thumbnails;
};

int peek_jpg_data(std::string dialFilePath, dial_thumbnail_info_t &dail_thumbnail_info, std::vector<uint8_t> &jpeg_data);

#endif //FUN_DIAL_PARSER_H
