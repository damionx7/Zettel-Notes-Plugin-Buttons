package org.eu.thedoc.zettelnotes.buttons.llm;

public enum LlmModel {

  GEMMA3_270M("gemma3-270m-it-q8.litertlm", "https://github.com/damionx7/HF-Models/releases/download/1/gemma3-270m-it-q8.litertlm",
      304L * 1024 * 1024),

  QWEN_0_5B("Qwen2.5-0.5B-Instruct_multi-prefill-seq_q8_ekv1280.task",
      "https://huggingface.co/litert-community/Qwen2.5-0.5B-Instruct/resolve/main/Qwen2.5-0.5B-Instruct_multi-prefill-seq_q8_ekv1280.task",
      547L * 1024 * 1024),

  QWEN_1_5B("Qwen2.5-1.5B-Instruct_seq128_q8_ekv1280.task",
      "https://huggingface.co/litert-community/Qwen2.5-1.5B-Instruct/resolve/main/Qwen2.5-1.5B-Instruct_seq128_q8_ekv1280.task",
      1570L * 1024 * 1024),

  QWEN_1_5B_4K("Qwen2.5-1.5B-Instruct_seq128_q8_ekv4096.task",
      "https://huggingface.co/litert-community/Qwen2.5-1.5B-Instruct/resolve/main/Qwen2.5-1.5B-Instruct_seq128_q8_ekv4096.task",
      1600L * 1024 * 1024);

  public static final LlmModel SMALL = GEMMA3_270M;
  public static final LlmModel BALANCED = QWEN_1_5B;
  public static final LlmModel LARGE = QWEN_1_5B_4K;

  public final String fileName;
  public final String downloadUrl;
  public final long approxBytes;

  LlmModel(String fileName, String downloadUrl, long approxBytes) {
    this.fileName = fileName;
    this.downloadUrl = downloadUrl;
    this.approxBytes = approxBytes;
  }
}

