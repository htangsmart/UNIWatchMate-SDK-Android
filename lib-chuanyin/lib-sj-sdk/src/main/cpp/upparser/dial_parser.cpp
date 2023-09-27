//
// Created by wangwenfeng on 2023/3/29.
//

#include <fstream>
#include <iostream>
#include "dial_parser.h"
#include <stdexcept>

/**
 *
 * @param dialFilePath
 * @param dail_thumbnail_info
 * @param jpeg_data
 * @return 0: success -1: file is not exist -2: format error -3:unknown error
 */
int peek_jpg_data(std::string dialFilePath, dial_thumbnail_info_t &dail_thumbnail_info, std::vector<uint8_t> &jpeg_data)
{
    std::ifstream file(dialFilePath, std::ios::binary);
    try {
        if (!file.is_open()) {
            return -1;
        }

        // 读取第五个整数
        file.seekg(0x20 + 4 * sizeof(uint32_t));
        uint32_t offset;
        file.read(reinterpret_cast<char *>(&offset), sizeof(uint32_t));

        // 读取 dial_thumbnail_info_t 结构体
        file.seekg(offset);
        file.read(reinterpret_cast<char *>(&dail_thumbnail_info.num), sizeof(uint16_t));

        if (dail_thumbnail_info.num != 1) {
            file.close();
            return -2;
        }

        dail_thumbnail_info.thumbnails.resize(dail_thumbnail_info.num);
        for (auto &thumbnail: dail_thumbnail_info.thumbnails) {
            file.read(reinterpret_cast<char *>(&thumbnail), sizeof(dial_thumbnail_t));
        }

        // 提取缩略图 JPEG 数据
        for (const auto &thumbnail: dail_thumbnail_info.thumbnails) {
            jpeg_data.resize(thumbnail.size);
            file.seekg(thumbnail.offset);
            file.read(reinterpret_cast<char *>(jpeg_data.data()), thumbnail.size);
            break;
        }

        file.close();
    }
    catch (const std::runtime_error& e) {
        std::cerr << "Error: " << e.what() << std::endl;
        if (file.is_open()) {
            file.close();
        }
        return -3;
    } catch (...) {
        std::cerr << "Unknown error occurred" << std::endl;
        if (file.is_open()) {
            file.close();
        }
        return -3;
    }
    return 0;
}
