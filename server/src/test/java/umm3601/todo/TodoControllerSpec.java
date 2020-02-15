package umm3601.todo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.javalin.core.validation.Validator;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;

import umm3601.Server;

/**
 * Tests the logic of the TodoController
 *
 * @throws IOException
 */
public class TodoControllerSpec {

  private Context ctx = mock(Context.class);

  private TodoController TodoController;
  private static TodoDatabase db;

  @BeforeEach
  public void setUp() throws IOException {
    ctx.clearCookieStore();

    db = new TodoDatabase(Server.TODO_DATA_FILE);
    TodoController = new TodoController(db);
  }

  @Test
  public void GET_to_request_all_todos() throws IOException {
    // Call the method on the mock controller
    TodoController. getTodos(ctx);

    // Confirm that `json` was called with all the todos.
    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    assertEquals(db.size(), argument.getValue().length);
  }

  @Test
  public void GET_to_request_limit_20_todos() throws IOException {
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("limit", Arrays.asList(new String[] { "20" }));

    when(ctx.queryParamMap()).thenReturn(queryParams);
    TodoController. getTodos(ctx);

    // Confirm that the todos passed to `json` have length 20.
    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    assertEquals(20, argument.getValue().length);
  }

  /**
   * Test that if the Todo sends a request with an illegal value in
   * the limit field (i.e., something that can't be parsed to a number)
   * we get a reasonable error code back.
   */
  @Test
  public void GET_to_request_todos_with_illegal_limit() {
    // We'll set the requested "limit" to be a string ("abc")
    // that can't be parsed to a number.
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("limit", Arrays.asList(new String[] { "abc" }));

    when(ctx.queryParamMap()).thenReturn(queryParams);
    // This should now throw a `BadRequestResponse` exception because
    // our request has an limit that can't be parsed to a number.
    Assertions.assertThrows(BadRequestResponse.class, () -> {
      TodoController.getTodos(ctx);
    });
  }

  @Test
  public void GET_to_request_sorted_owner_todos() throws IOException {

    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("orderBy", Arrays.asList(new String[] { "owner" }));

    when(ctx.queryParamMap()).thenReturn(queryParams);
    TodoController. getTodos(ctx);

    // Confirm that all the todos passed to `json` are sorted by owner.
    ArgumentCaptor<Todo[]> argument = ArgumentCaptor.forClass(Todo[].class);
    verify(ctx).json(argument.capture());
    for (int i = 0; i < argument.getValue().length - 1; ++i) {
      if (argument.getValue()[i].owner.compareTo(argument.getValue()[i + 1].owner) > 0)  {
        Assertions.fail();
      }
    }
  }

  /**
   * Test that if the Todo sends a request with an illegal value in
   * the orderBy field (i.e., an non-applicable todo attribute)
   * we get a reasonable error code back.
   */
  @Test
  public void GET_to_request_todos_with_illegal_order() {
    // We'll set the requested "age" to be a string ("abc")
    // that can't be parsed to a number.
    Map<String, List<String>> queryParams = new HashMap<>();
    queryParams.put("orderBy", Arrays.asList(new String[] { "unknown" }));

    when(ctx.queryParamMap()).thenReturn(queryParams);
    // This should now throw a `BadRequestResponse` exception because
    // our request has an order that is not an applicable todo attribute.
    Assertions.assertThrows(BadRequestResponse.class, () -> {
      TodoController.getTodos(ctx);
    });
  }

  @Test
  public void GET_to_request_user_with_existent_id() throws IOException {
    when(ctx.pathParam("id", String.class)).thenReturn(new Validator<String>("58895985a22c04e761776d54", ""));
    TodoController.getTodo(ctx);
    verify(ctx).status(201);
  }

  @Test
  public void GET_to_request_user_with_nonexistent_id() throws IOException {
    when(ctx.pathParam("id", String.class)).thenReturn(new Validator<String>("nonexistent", ""));
    Assertions.assertThrows(NotFoundResponse.class, () -> {
      TodoController.getTodo(ctx);
    });
  }
}
