package org.eu.thedoc.zettelnotes.buttons.llm;

public enum LlmAction {
  SUMMARIZE("Summarize"),
  REPHRASE("Improve clarity"),
  FIX_GRAMMAR("Fix grammar"),
  GENERATE_TAGS("Generate tags"),
  EXPAND("Expand into paragraph");

  public final String label;

  LlmAction(String label) {
    this.label = label;
  }

  public String buildPrompt(String selectedText) {
    if (selectedText == null || selectedText.isBlank()) {
      return "";
    }

    return switch (this) {
      case SUMMARIZE -> String.format(

          """
          You are a summarization assistant. Produce a faithful, concise summary.
          
          Instructions:
          - Summarize the text in exactly 3 sentences.
          - Keep only the key ideas; do not add information or interpretations.
          - Preserve the original meaning and tone.
          - Return only the summary text, no extra commentary.
          
          Text to summarize:
          ```
          %s
          ```
          """, selectedText);
      case REPHRASE -> String.format(

          """
          You are a clarity editor. Rewrite the text to be clearer, more natural, and concise.
          
          Instructions:
          - Preserve the exact meaning and all important information.
          - Improve flow, wording, and readability.
          - Do not add or remove facts.
          - Return only the rewritten text.
          
          Text to rewrite:
          ```
          %s
          ```
          """, selectedText);
      case FIX_GRAMMAR -> String.format(

          """
          You are a proofreading assistant. Correct surface errors with minimal changes.
          
          Instructions:
          - Fix grammar, spelling, punctuation, and obvious wording issues.
          - Preserve the original meaning, formatting, Markdown, and technical terms.
          - Make the smallest changes needed to make the text correct.
          - If the text is already correct, return it unchanged.
          - Return only the corrected text.
          
          Text to proofread:
          ```
          %s
          ```
          """, selectedText);
      case GENERATE_TAGS -> String.format(

          """
          You are a tagging assistant. Generate concise topic tags for the text.
          
          Instructions:
          - Produce 3 to 5 lowercase tags that describe the main topics.
          - Use single words or short phrases (1–3 words each).
          - Separate tags with commas, no spaces after commas.
          - Do not use #, do not add explanations, and do not include any other text.
          - If no clear topics can be identified, return an empty string.
          
          Text to tag:
          ```
          %s
          ```
          """, selectedText);
      case EXPAND -> String.format(

          """
          You are a writing assistant. Expand a note into a coherent paragraph.
          
          Instructions:
          - Turn the bullet/note into one clear, well-written paragraph.
          - Preserve the original idea; do not invent facts or details.
          - Maintain a neutral, professional tone.
          - Return only the expanded paragraph.
          
          Text to expand:
          ```
          %s
          ```
          """, selectedText);
    };
  }
}