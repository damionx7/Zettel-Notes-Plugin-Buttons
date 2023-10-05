import java.util.List;
import org.eu.thedoc.zettelnotes.plugins.alarm.Regex;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.AlarmModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RegexTest {

  private Regex mRegex;

  @BeforeEach
  public void setUp() {
    mRegex = Regex.getInstance();
  }

  @Test
  public void getModelsTest() {
    List<AlarmModel> models = mRegex.getModels("App Folder", "/index.md", " [scheduled]: <2023-09-01 09:12 .+1w>\nThis is text.");
    Assertions.assertEquals(1, models.size());
    Assertions.assertEquals("This is text.", models.get(0).getText());
  }

}
