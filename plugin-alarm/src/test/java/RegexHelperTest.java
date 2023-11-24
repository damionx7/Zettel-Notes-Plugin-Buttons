import java.util.List;
import org.eu.thedoc.zettelnotes.plugins.alarm.database.AlarmModel;
import org.eu.thedoc.zettelnotes.plugins.alarm.utils.RegexHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RegexHelperTest {

  @Test
  public void getModelsTest() {
    List<AlarmModel> models = RegexHelper.parse("App Folder", "/index.md", "", "[scheduled]: <2023-09-01 09:12 +1w>\nThis is text.");
    Assertions.assertEquals(1, models.size());
    Assertions.assertEquals("[scheduled]: <2023-09-01 09:12 +1w>\nThis is text.", models.get(0).getText());
    Assertions.assertEquals("+1w", models.get(0).getRecurrence());
  }

}
