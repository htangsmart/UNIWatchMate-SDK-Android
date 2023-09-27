package com.sjbt.sdk.utils;

import com.sjbt.sdk.entity.SportInitInfo;

import java.text.DecimalFormat;

public class StepCalculateUtil {

    /**
     * 获取距离
     *
     * @param stepCount
     * @return 步长：step_length = 0.37 * height(米) // 身高* 0.37
     * 如果没有身高，step_length = 0.6米
     * <p>
     * 距离：distance = step_length * step_count / 1000 单位：km
     * 卡路里: Calories = weight(kg) * distance * 1.036
     */
    public static float getDistance(int stepCount, SportInitInfo initInfo) {

        float stepLength = initInfo.getHeight() * 0.37f / 100;

        if (stepLength <= 0) {
            stepLength = 0.60f;
        }

        return formatFloat((stepLength * stepCount / 1000));
    }

    /**
     * 获取热量
     *
     * @param stepCount
     * @return
     */
    public static float getHeat(int stepCount, SportInitInfo initInfo) {
        return formatFloat((getDistance(stepCount, initInfo) * initInfo.getWeight() * 1.036f));
    }

    public static float formatFloat(Float num) {
        DecimalFormat df = new DecimalFormat("#.00");
        String str = df.format(num);
        return Float.parseFloat(str);
    }

    /**
     * @param step_count
     * @return
     */

    public static float getDistanceByStep(int step_count, SportInitInfo initInfo) {
        float distance = 0;
        int height = initInfo.getHeight();

        if (step_count >= 10000) {
            if (height != 0) {
                distance = step_count / 10000 * 37 * height / 10;
            } else {
                distance = step_count / 10000 * 600;
            }

            step_count = step_count % 10000;
        }

        if (height != 0) {
            distance += step_count * 37 * height / 100000;
        } else {
            distance += step_count * 6 / 100;
        }

        return formatFloat(distance);
    }

    public static float getHeatByStep(int step_count, SportInitInfo initInfo) {
        float calories = 0;
        float distance = getDistanceByStep(step_count, initInfo);
        int weight = initInfo.getWeight();

        if (calories != 0 && weight > 0) {
            calories = 0;
            // 距离大于10km, 计算10km整倍数的卡路里
            if (distance >= 10 * 100) {
                calories = (10 * weight * 1036 / 10) * (distance / (10 * 100));
            }
            calories += (distance % (10 * 100)) * weight * 1036 / 1000;
        }

        return formatFloat(calories);
    }
}
