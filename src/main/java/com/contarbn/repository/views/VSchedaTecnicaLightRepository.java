package com.contarbn.repository.views;

import com.contarbn.model.views.VSchedaTecnicaLight;
import com.contarbn.repository.custom.VSchedaTecnicaLightCustomRepository;
import org.springframework.data.repository.CrudRepository;

public interface VSchedaTecnicaLightRepository extends CrudRepository<VSchedaTecnicaLight, Long>, VSchedaTecnicaLightCustomRepository {

}
