package com.laptophub.user.controller;

import com.laptophub.shared.response.ApiResponse;
import com.laptophub.user.dto.request.AddressCreateRequest;
import com.laptophub.user.dto.request.AddressUpdateRequest;
import com.laptophub.user.dto.response.AddressResponse;
import com.laptophub.user.entity.Address;
import com.laptophub.user.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer/addresses")
public class CustomerAddressController {

    private final AddressService addressService;

    public CustomerAddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> list(){
        List<AddressResponse> responses = addressService.list().stream()
                .map(AddressResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Danh địa chỉ Khách hành",responses));
    }
    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> create(@Valid @RequestBody AddressCreateRequest request) {
        Address address = addressService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Thêm địa chỉ thành công!",AddressResponse.from(address)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> getOne(@PathVariable Long id) {
        Address address = addressService.getOwned(id);
        return ResponseEntity.ok(ApiResponse.success("Ok",AddressResponse.from(address)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> update(@PathVariable Long id,
                                                               @Valid @RequestBody AddressUpdateRequest request) {
        Address address = addressService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật địa chỉ thành công!",AddressResponse.from(address)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefault(@PathVariable Long id) {
        Address address = addressService.setDefault(id);
        return ResponseEntity.ok(ApiResponse.success("Ok",AddressResponse.from(address)));
    }
}
