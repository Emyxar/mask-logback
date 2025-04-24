# Аннотируем нужные поля для маскирования
* Реализация через OpenAPI
```
components:
  schemas:
    User:
      type: object
      properties:
        name:
          type: string
          x-field-extra-annotation: "@ru.evg.mask_logback.annotations.MaskingField"
        login:
          type: string
          x-field-extra-annotation: "@ru.evg.mask_logback.annotations.MaskingField"
        post:
          type: string
          x-field-extra-annotation: "@ru.evg.mask_logback.annotations.MaskingField"
        series:
          type: string
          x-field-extra-annotation: "@ru.evg.mask_logback.annotations.MaskingField"
        number:
          type: string
          x-field-extra-annotation: "@ru.evg.mask_logback.annotations.MaskingField"
    Order:
      type: object
      properties:
        bisId:
          type: array
          items:
            type: string
        id:
          type: string
        type:
          type: string
          x-field-extra-annotation: "@ru.evg.mask_logback.annotations.MaskingField"
        user:
          items:
            $ref: "#/components/schemas/User"
    OrderResponse:
      type: object
      properties:
        orders:
          type: array
          items:
            $ref: "#/components/schemas/Order"
```


## Результат маскирования
```
17:17:51.041 [http-nio-8080-exec-1] INFO  r.e.m.controllers.Controller - Ответ: {
  "orders": [
    {
      "bisId": [
        "Test1ID",
        "Test2ID"
      ],
      "id": "test",
      "type": "****",
      "user": [
        {
          "name": "******",
          "login": "*****",
          "post": "*****",
          "series": "****",
          "number": "********"
        },
        {
          "name": "******",
          "login": "*****",
          "post": "*****",
          "series": "****",
          "number": "********"
        }
      ]
    }
  ]
}
```
