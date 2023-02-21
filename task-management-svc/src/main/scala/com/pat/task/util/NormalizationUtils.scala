package com.pat.task.util

object NormalizationUtils {
  /**
   * 规格化参数
   *
   * @param progress
   * @return
   */
  def normalizeProgress(progress: Double): Double = if (progress < 0) 0 else if (progress > 1) 1 else progress
}
