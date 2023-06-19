package com.example.auction.controller;

import com.example.auction.dto.*;
import com.example.auction.enums.LotStatus;
import com.example.auction.service.AuctionService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lot")
public class AuctionController {

    private final AuctionService auctionService;

    public AuctionController(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    @GetMapping("/{id}/first")
    @Operation(summary = "Получить информацию о первом ставившем на лот", description = "Возвращает первого ставившего на этот лот")
    public ResponseEntity<String> getFirstBidder(@PathVariable("id") int lotId) {
        ResponseEntity<String> response;
        String result = auctionService.getFirstBidder(lotId);
        response = switch (result) {
            case "Лот не найден", "Заявок по этому лоту нет" -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            case "Лот в неверном статусе" -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            default -> ResponseEntity.status(HttpStatus.OK).body(result);
        };
        return response;
    }

    @GetMapping("/{id}/frequent")
    @Operation(summary = "Возвращает имя ставившего на данный лот наибольшее количество раз", description = "Наибольшее количество вычисляется из общего количества ставок на лот")
    public ResponseEntity<String> getMostFrequentBidder(@PathVariable("id") int lotId) {
        ResponseEntity<String> response;
        String result = auctionService.getMostFrequentBidder(lotId);
        response = switch (result) {
            case "Лот не найден", "Заявок по этому лоту нет", "Не удалось определить наиболее активного участника" -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            default -> ResponseEntity.status(HttpStatus.OK).body(result);
        };
        return response;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить полную информацию о лоте", description = "Возвращает полную информацию о лоте с последним ставившим и текущей ценой")
    public ResponseEntity<?> getFullLotById(@PathVariable("id") int lotId) {
        FullLotDTO fullLotDTO = auctionService.getFullLotById(lotId);
        return fullLotDTO.getId() != 0 ? ResponseEntity.ok(fullLotDTO) : new ResponseEntity<>("Лот не найден", HttpStatus.NOT_FOUND);
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Начать торги по лоту", description = """
            Переводит лот в состояние "начато", которое позволяет делать ставки на лот.
            Если лот уже находится в состоянии "начато", то ничего не делает и возвращает 200""")
    public ResponseEntity<?> startLot(@PathVariable("id") int lotId) {
        boolean started = auctionService.startLot(lotId);
        return started ? ResponseEntity.ok().build() : new ResponseEntity<>("Лот не найден", HttpStatus.NOT_FOUND);
    }

    @PostMapping("/bid")
    @Operation(summary = "Сделать ставку по лоту", description = """
            Создает новую ставку по лоту.
            Если лот в статусе CREATED или STOPPED, то должна вернутся ошибка""")
    public ResponseEntity<String> createBid(@RequestParam("Id") int lotId, @RequestBody CreationBidDTO creationBidDTO) {
        ResponseEntity<String> response;
        String result = auctionService.createBid(lotId, creationBidDTO);
        response = switch (result) {
            case "Лот не найден" -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            case "Лот в неверном статусе" -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            default -> ResponseEntity.status(HttpStatus.OK).body(result + " для " + creationBidDTO.getBidderName());
        };
        return response;
    }

    @PostMapping("/{id}/stop")
    @Operation(summary = "Остановить торги по лоту", description = """
            Переводит лот в состояние "остановлен", которое запрещает делать ставки на лот.
            Если лот уже находится в состоянии "остановлен", то ничего не делает и возвращает 200""")
    public ResponseEntity<?> stopLot(@PathVariable("id") int lotId) {
        boolean stopped = auctionService.stopLot(lotId);
        return stopped ? ResponseEntity.ok().build() : new ResponseEntity<>("Лот не найден", HttpStatus.NOT_FOUND);
    }

    @PostMapping
    @Operation(summary = "Создает новый лот", description = """
            Метод создания нового лота,
            если есть ошибки в полях объекта лота - то нужно вернуть статус с ошибкой""")
    public ResponseEntity<LotDto> createLot(@RequestBody CreationLotDTO creationLotDTO) {
        LotDto lotDto = auctionService.createLot(creationLotDTO);
        return ResponseEntity.ok(lotDto);
    }

    @GetMapping
    @Operation(summary = "Получить все лоты, основываясь на фильтре статуса и номере страницы", description = """
            Возвращает все записи о лотах без информации о текущей цене и победителе
            постранично.
            Если страница не указана, то возвращается первая страница.
            Номера страниц начинаются с 0.
            Лимит на количество лотов на странице - 10 штук.""")
    public ResponseEntity<Page<LotDto>> findLots(@RequestParam(value = "status", defaultValue = "CREATED") LotStatus status,
                                                 @RequestParam(value = "page", required = false, defaultValue = "0") int page) {
        Page<LotDto> lots = auctionService.findLotsByStatus(status, page);
        return ResponseEntity.ok(lots);
    }

    @GetMapping(value = "/export", produces = "application/csv")
    @Operation(summary = "Экспортировать все лоты в файл CSV", description = """
            Экспортировать все лоты в формате
            id, title, status, lastBidder, currentPrice
            в одном файле CSV""")
    public ResponseEntity<byte[]> exportLotsToCSV() {
        byte[] csvData = auctionService.exportLotsToCSV();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/csv"));
        headers.setContentDispositionFormData("attachment", "lots.csv");
        headers.setContentLength(csvData.length);
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }
}

