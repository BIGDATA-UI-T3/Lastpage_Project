// [수정] 파일이 새 폴더로 이동했으므로 package 경로를 수정합니다.
package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.GoodsReserve;
import org.springframework.data.jpa.repository.JpaRepository;

// @Repository 어노테이션을 붙여주는 것이 좋습니다.
import org.springframework.stereotype.Repository;

@Repository
public interface GoodsReserveRepository extends JpaRepository <GoodsReserve, Long> {
}